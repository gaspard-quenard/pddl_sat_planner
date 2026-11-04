package fr.uga.pddl4j.examples.sat;

import fr.uga.pddl4j.problem.Fluent;
import fr.uga.pddl4j.problem.InitialState;
import fr.uga.pddl4j.problem.Problem;
import fr.uga.pddl4j.problem.operator.Action;
import fr.uga.pddl4j.problem.operator.Condition;
import fr.uga.pddl4j.problem.operator.Effect;
import fr.uga.pddl4j.util.BitVector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper functions showing how to access the fluents, the actions, the initial
 * state and the goal of a problem, and how to display them.
 *
 * <p>
 * Reminder: in PDDL4J, fluents and actions are identified by their index in
 * problem.getFluents() and problem.getActions(). Sets of fluents (preconditions,
 * effects, initial state, goal) are BitVectors: bit i is set when the fluent
 * problem.getFluents().get(i) is in the set.
 * </p>
 */
public final class Helpers {

    /**
     * The class logger.
     */
    private static final Logger LOGGER = LogManager.getLogger(Helpers.class.getName());

    private Helpers() {
    }

    /**
     * Converts a fluent into a readable string (for example: "at r1 l1").
     *
     * @param f       the fluent.
     * @param problem the problem.
     * @return the readable string.
     */
    public static String fluentToString(Fluent f, Problem problem) {
        StringBuilder str = new StringBuilder();
        // The fluent name (e.g. "at" for the fluent at ?r - robot ?l - location)
        str.append(problem.getPredicateSymbols().get(f.getSymbol()));
        // Then its arguments
        for (int arg : f.getArguments()) {
            str.append(" ").append(problem.getConstantSymbols().get(arg));
        }
        return str.toString();
    }

    /**
     * Converts an action into a readable string (for example: "move r1 l1 l2").
     *
     * @param a       the action.
     * @param problem the problem.
     * @return the readable string.
     */
    public static String actionToString(Action a, Problem problem) {
        StringBuilder str = new StringBuilder();
        // The action name (e.g. "move" for the action move r1 l1 l2)
        str.append(a.getName());
        // Then its arguments
        for (int arg : a.getInstantiations()) {
            str.append(" ").append(problem.getConstantSymbols().get(arg));
        }
        return str.toString();
    }

    /**
     * Returns the indices of the fluents contained in a BitVector, i.e. the
     * indices of its bits set to 1.
     *
     * @param bits the BitVector (e.g. initialState.getPositiveFluents()).
     * @return the list of the indices of the fluents.
     */
    public static List<Integer> getFluentIndices(BitVector bits) {
        List<Integer> indices = new ArrayList<>();
        // nextSetBit(i) returns the first bit set to 1 from i (included), -1 if none
        for (int i = bits.nextSetBit(0); i >= 0; i = bits.nextSetBit(i + 1)) {
            indices.add(i);
        }
        return indices;
    }

    /**
     * Returns the fluents contained in a BitVector.
     *
     * @param bits    the BitVector (e.g. initialState.getPositiveFluents()).
     * @param problem the problem.
     * @return the list of the fluents.
     */
    public static List<Fluent> getFluents(BitVector bits, Problem problem) {
        List<Fluent> fluents = new ArrayList<>();
        for (int i : getFluentIndices(bits)) {
            fluents.add(problem.getFluents().get(i));
        }
        return fluents;
    }

    /**
     * Returns the fluents that are false in the initial state. In PDDL, the
     * initial state only lists the true fluents (closed world assumption): every
     * fluent that is not positive in the initial state is false.
     *
     * @param problem the problem.
     * @return the BitVector of the fluents that are false in the initial state.
     */
    public static BitVector getNegativeFluentsOfInitialState(Problem problem) {
        BitVector negative = new BitVector();
        negative.set(0, problem.getFluents().size()); // all the fluents ...
        negative.andNot(problem.getInitialState().getPositiveFluents()); // ... minus the positive ones
        return negative;
    }

    /**
     * Logs a set of fluents, one per line.
     *
     * @param title   the title to display before the fluents.
     * @param bits    the BitVector of the fluents to display.
     * @param problem the problem.
     */
    private static void logFluents(String title, BitVector bits, Problem problem) {
        LOGGER.info("{} ({}):\n", title, bits.cardinality());
        for (int i : getFluentIndices(bits)) {
            LOGGER.info("    [{}] {}\n", i, fluentToString(problem.getFluents().get(i), problem));
        }
    }

    /**
     * Displays information about the problem: number of fluents and actions, some
     * fluents and actions (with their preconditions and effects), the initial
     * state and the goal.
     *
     * @param problem the problem.
     */
    public static void printProblemInfo(Problem problem) {
        final int nbDisplayed = 5;

        LOGGER.info("Number of fluents: {}\n", problem.getFluents().size());
        LOGGER.info("Number of actions: {}\n", problem.getActions().size());

        // Fluents: problem.getFluents().get(i) is the fluent of index i
        LOGGER.info("Some fluents of the problem:\n");
        for (int i = 0; i < Math.min(nbDisplayed, problem.getFluents().size()); i++) {
            LOGGER.info("    [{}] {}\n", i, fluentToString(problem.getFluents().get(i), problem));
        }

        // Actions: problem.getActions().get(i) is the action of index i
        LOGGER.info("Some actions of the problem:\n");
        for (int i = 0; i < Math.min(nbDisplayed, problem.getActions().size()); i++) {
            Action a = problem.getActions().get(i);
            LOGGER.info("[{}] {}\n", i, actionToString(a, problem));

            Condition pre = a.getPrecondition();
            logFluents("  Positive preconditions", pre.getPositiveFluents(), problem);
            logFluents("  Negative preconditions", pre.getNegativeFluents(), problem);

            Effect eff = a.getUnconditionalEffect();
            logFluents("  Positive effects (add)", eff.getPositiveFluents(), problem);
            logFluents("  Negative effects (del)", eff.getNegativeFluents(), problem);
        }

        // Initial state
        InitialState init = problem.getInitialState();
        logFluents("Initial state: true fluents", init.getPositiveFluents(), problem);
        logFluents("Initial state: false fluents", getNegativeFluentsOfInitialState(problem), problem);

        // Goal
        Condition goal = problem.getGoal();
        logFluents("Goal: positive fluents", goal.getPositiveFluents(), problem);
        logFluents("Goal: negative fluents", goal.getNegativeFluents(), problem);
    }
}
