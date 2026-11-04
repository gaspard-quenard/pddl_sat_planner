package fr.uga.pddl4j.examples.sat;

import fr.uga.pddl4j.parser.DefaultParsedProblem;
import fr.uga.pddl4j.plan.Plan;
import fr.uga.pddl4j.planners.AbstractPlanner;
import fr.uga.pddl4j.problem.DefaultProblem;
import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.operator.Action;
import fr.uga.pddl4j.problem.operator.ConditionalEffect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.sat4j.core.Vec;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.IVecInt;
import org.sat4j.specs.TimeoutException;
import picocli.CommandLine;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * A planner that encodes a planning problem into a SAT formula (see
 * {@link SATEncoding}), solves it with SAT4J, decodes the model into a plan and
 * checks this plan with VAL.
 *
 * <p>
 * The planner searches for a plan of increasing maximum size n (1, 2, 4, 8, ...)
 * until the formula is satisfiable or the timeout is reached.
 * </p>
 */
@CommandLine.Command(name = "SAT",
    version = "SAT 1.0",
    description = "Solves a specified planning problem by encoding it into a SAT formula and calling a SAT solver.",
    sortOptions = false,
    mixinStandardHelpOptions = true,
    headerHeading = "Usage:%n",
    synopsisHeading = "%n",
    descriptionHeading = "%nDescription:%n%n",
    parameterListHeading = "%nParameters:%n",
    optionListHeading = "%nOptions:%n")
public class SAT extends AbstractPlanner {

    /**
     * The class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(SAT.class.getName());

    /**
     * Path to the VAL "Validate" executable used to check the plan found.
     */
    @CommandLine.Option(names = {"-V", "--validator"}, defaultValue = "VAL/Validate",
        paramLabel = "<path>", description = "Path to the VAL Validate executable (default: ${DEFAULT-VALUE}).")
    private String validatorPath;

    /**
     * Whether the plan found must be validated with VAL.
     */
    @CommandLine.Option(names = {"--no-validation"}, description = "Do not validate the plan found with VAL.")
    private boolean noValidation = false;

    /**
     * Whether the information about the problem must be displayed before solving.
     */
    @CommandLine.Option(names = {"-i", "--info"},
        description = "Display information about the problem (fluents, actions, initial state, goal).")
    private boolean showInfo = false;

    /**
     * Instantiates the planning problem from a parsed problem.
     *
     * @param problem the problem to instantiate.
     * @return the instantiated planning problem or null if the problem cannot be
     *         instantiated.
     */
    @Override
    public Problem instantiate(DefaultParsedProblem problem) {
        final Problem pb = new DefaultProblem(problem);
        pb.instantiate();
        return pb;
    }

    /**
     * Returns whether the problem is supported by this planner: we only handle
     * actions whose effects are unconditional (STRIPS-like problems).
     *
     * @param problem the problem to test.
     * @return true if the problem is supported, false otherwise.
     */
    @Override
    public boolean isSupported(Problem problem) {
        for (Action a : problem.getActions()) {
            for (ConditionalEffect ce : a.getConditionalEffects()) {
                if (!ce.getCondition().isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Uses SAT4J to check whether the formula is satisfiable and to find a model.
     * See https://sat4j.gitbooks.io/case-studies/content/using-sat4j-as-a-java-library.html
     *
     * @param clauses   the formula in CNF.
     * @param nbVars    the number of variables of the formula.
     * @param timeoutMs the time allowed to the solver, in milliseconds.
     * @return the model (for each variable v: v if v is true, -v if v is false) if
     *         the formula is satisfiable, null otherwise.
     * @throws TimeoutException if the solver did not finish in time.
     */
    private int[] solveSAT(Vec<IVecInt> clauses, int nbVars, long timeoutMs) throws TimeoutException {
        ISolver solver = SolverFactory.newDefault();
        solver.newVar(nbVars);
        solver.setExpectedNumberOfClauses(clauses.size());
        solver.setTimeoutMs(timeoutMs);

        try {
            solver.addAllClauses(clauses);
        } catch (ContradictionException e) {
            // The formula is trivially unsatisfiable (e.g. an empty clause)
            return null;
        }
        return solver.isSatisfiable() ? solver.model() : null;
    }

    /**
     * Searches a solution plan for the problem using a SAT solver. The maximum size
     * of the plan is doubled until the formula is satisfiable or the timeout is
     * reached.
     *
     * @param problem the problem to solve.
     * @return the plan found or null if no plan was found.
     */
    @Override
    public Plan solve(final Problem problem) {
        if (showInfo) {
            Helpers.printProblemInfo(problem);
        }

        final SATEncoding encoding = new SATEncoding(problem);
        final long deadline = System.currentTimeMillis() + this.getTimeout() * 1000L;
        int sizePlan = 1;
        int[] model = null;

        while (model == null) {
            // Encode the problem
            LOGGER.info("Encoding the problem for a plan of maximum size {}\n", sizePlan);
            final long beginEncode = System.currentTimeMillis();
            final Vec<IVecInt> clauses = encoding.encode(sizePlan);
            final int nbVars = encoding.getNumberOfVariables(sizePlan);
            final long endEncode = System.currentTimeMillis();
            this.getStatistics().setTimeToEncode(this.getStatistics().getTimeToEncode() + endEncode - beginEncode);
            LOGGER.info("{} variables, {} clauses\n", nbVars, clauses.size());

            // Solve it
            final long remaining = deadline - System.currentTimeMillis();
            if (remaining <= 0) {
                LOGGER.info("Timeout\n");
                return null;
            }
            try {
                model = solveSAT(clauses, nbVars, remaining);
            } catch (TimeoutException e) {
                LOGGER.info("Timeout\n");
                return null;
            } finally {
                this.getStatistics().setTimeToSearch(
                    this.getStatistics().getTimeToSearch() + System.currentTimeMillis() - endEncode);
            }

            if (model == null) {
                LOGGER.info("No plan of size {}\n", sizePlan);
                sizePlan *= 2;
            }
        }

        LOGGER.info("The formula is satisfiable for a plan of maximum size {}\n", sizePlan);
        final Plan plan = encoding.decode(model, sizePlan);

        if (!noValidation) {
            validatePlan(plan, problem);
        }
        return plan;
    }

    // ==================================================================================
    // PLAN VALIDATION WITH VAL
    // ==================================================================================

    /**
     * Writes the plan in the VAL format: one action per line, e.g. "(move r1 l1 l2)".
     *
     * @param plan    the plan.
     * @param problem the problem.
     * @param file    the file to write.
     * @throws IOException if the file cannot be written.
     */
    private void writePlan(Plan plan, Problem problem, File file) throws IOException {
        List<String> lines = new ArrayList<>();
        for (Action a : plan.actions()) {
            lines.add("(" + Helpers.actionToString(a, problem) + ")");
        }
        Files.write(file.toPath(), lines, StandardCharsets.UTF_8);
    }

    /**
     * Checks the plan with the VAL Validate executable and logs the result.
     *
     * @param plan    the plan to check.
     * @param problem the problem.
     * @return true if VAL says the plan is valid, false otherwise (or if VAL could
     *         not be run).
     */
    private boolean validatePlan(Plan plan, Problem problem) {
        File validator = new File(validatorPath);
        if (!validator.canExecute()) {
            LOGGER.warn("VAL not found or not executable at '{}': plan not validated (see option --validator)\n",
                validatorPath);
            return false;
        }

        File planFile = null;
        try {
            planFile = File.createTempFile("plan", ".txt");
            writePlan(plan, problem, planFile);

            ProcessBuilder pb = new ProcessBuilder(validator.getAbsolutePath(),
                this.getDomain(), this.getProblem(), planFile.getAbsolutePath());
            pb.redirectErrorStream(true);
            Process process = pb.start();

            StringBuilder output = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    output.append(line).append("\n");
                }
            }
            final boolean valid = process.waitFor() == 0 && output.toString().contains("Plan valid");

            if (valid) {
                LOGGER.info("VAL: the plan is VALID\n");
            } else {
                LOGGER.error("VAL: the plan is INVALID\n{}", output);
            }
            return valid;
        } catch (IOException | InterruptedException e) {
            LOGGER.error("Cannot run VAL: {}\n", e.getMessage());
            return false;
        } finally {
            if (planFile != null) {
                planFile.delete();
            }
        }
    }

    /**
     * The main method of the <code>SAT</code> planner.
     *
     * @param args the arguments of the command line.
     */
    public static void main(String[] args) {
        try {
            final SAT planner = new SAT();
            CommandLine cmd = new CommandLine(planner);
            cmd.execute(args);
        } catch (IllegalArgumentException e) {
            LOGGER.fatal(e.getMessage());
        }
    }
}
