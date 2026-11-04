package fr.uga.pddl4j.examples.sat;

import fr.uga.pddl4j.plan.Plan;
import fr.uga.pddl4j.plan.SequentialPlan;
import fr.uga.pddl4j.problem.Problem;
import org.sat4j.core.Vec;
import org.sat4j.core.VecInt;
import org.sat4j.specs.IVecInt;

/**
 * Encoding of a planning problem into a SAT formula (CNF), and decoding of a
 * model of this formula into a plan.
 *
 * <p>
 * The formula for a plan of maximum size n must be satisfiable if and only if
 * there is a plan of at most n steps. Each fluent at each time step and each
 * action at each time step must be represented by a SAT variable.
 * </p>
 *
 * <p>
 * SAT4J reminders:
 * </p>
 * <ul>
 * <li>a variable is a strictly positive integer (1, 2, 3, ...): 0 is not a
 * valid variable;</li>
 * <li>a literal is a variable x (x is true) or its opposite -x (x is false);</li>
 * <li>a clause is an IVecInt: new VecInt(new int[] {x, -y, z}) means
 * (x OR NOT y OR z);</li>
 * <li>add a clause to the formula with: clauses.push(new VecInt(new int[] {...}));</li>
 * <li>a model returned by the solver is an int[] containing, for each variable
 * v, v if v is true and -v if v is false.</li>
 * </ul>
 *
 * <p>
 * See {@link Helpers} to know how to access the fluents, the actions, the
 * initial state and the goal of the problem.
 * </p>
 *
 * <p>
 * TODO (students): complete all the methods of this class.
 * </p>
 */
public class SATEncoding {

    /**
     * The problem to encode.
     */
    private final Problem problem;

    /**
     * Creates the encoding of a problem.
     *
     * @param problem the problem to encode.
     */
    public SATEncoding(Problem problem) {
        this.problem = problem;
    }

    // ==================================================================================
    // VARIABLES OF THE FORMULA
    // ==================================================================================

    /**
     * Returns the number of variables of the formula for a plan of maximum size
     * sizePlan (used to initialize the SAT solver).
     *
     * @param sizePlan the maximum size of the plan.
     * @return the number of variables.
     */
    public int getNumberOfVariables(int sizePlan) {
        // TODO
        return 0;
    }

    // ==================================================================================
    // ENCODING
    // ==================================================================================

    /**
     * Encodes the initial state.
     *
     * @param clauses the formula to which the clauses are added.
     */
    private void encodeInitialState(Vec<IVecInt> clauses) {
        // TODO
    }

    /**
     * Encodes the goal.
     *
     * @param sizePlan the maximum size of the plan.
     * @param clauses  the formula to which the clauses are added.
     */
    private void encodeGoal(int sizePlan, Vec<IVecInt> clauses) {
        // TODO
    }

    /**
     * Encodes the actions: their preconditions and their effects.
     *
     * @param sizePlan the maximum size of the plan.
     * @param clauses  the formula to which the clauses are added.
     */
    private void encodeActions(int sizePlan, Vec<IVecInt> clauses) {
        // TODO
    }

    /**
     * Encodes the explanatory frame axioms: if a fluent changes its value between
     * two consecutive time steps, an action that can cause this change was
     * executed.
     *
     * @param sizePlan the maximum size of the plan.
     * @param clauses  the formula to which the clauses are added.
     */
    private void encodeFrameAxioms(int sizePlan, Vec<IVecInt> clauses) {
        // TODO
    }

    /**
     * Encodes the mutual exclusion of the actions: at most one action is executed
     * at each time step.
     *
     * @param sizePlan the maximum size of the plan.
     * @param clauses  the formula to which the clauses are added.
     */
    private void encodeActionMutex(int sizePlan, Vec<IVecInt> clauses) {
        // TODO
    }

    /**
     * Encodes the problem into a CNF formula that is satisfiable if and only if
     * there is a plan of at most sizePlan steps.
     *
     * @param sizePlan the maximum size of the plan.
     * @return the clauses of the formula.
     */
    public Vec<IVecInt> encode(int sizePlan) {
        Vec<IVecInt> clauses = new Vec<>();
        encodeInitialState(clauses);
        encodeGoal(sizePlan, clauses);
        encodeActions(sizePlan, clauses);
        encodeFrameAxioms(sizePlan, clauses);
        encodeActionMutex(sizePlan, clauses);
        return clauses;
    }

    // ==================================================================================
    // DECODING
    // ==================================================================================

    /**
     * Builds the plan from a model of the formula.
     * Use plan.add(timeStep, action) to add an action to the plan.
     *
     * @param model    the model returned by the SAT solver.
     * @param sizePlan the maximum size of the plan.
     * @return the plan.
     */
    public Plan decode(int[] model, int sizePlan) {
        Plan plan = new SequentialPlan();
        // TODO
        return plan;
    }
}
