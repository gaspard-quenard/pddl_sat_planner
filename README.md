# pddl_sat_planner

Goal: complete all the methods of `SATEncoding.java` (choice of the SAT
variables, encoding of the problem into CNF, and construction of the plan from
the model returned by the solver).

## Source files (`src/fr/uga/pddl4j/examples/sat/`)

- `SAT.java`: the planner (main loop, call to SAT4J, plan validation with VAL).
  Nothing to modify.
- `SATEncoding.java`: **to complete**.
- `Helpers.java`: how to access the fluents, actions, initial state and goal of
  a problem, and how to display them.

## Content

- `lib/pddl4j-4.0.0.jar`: PDDL parser / problem instantiation.
- `lib/org.ow2.sat4j.core-2.3.6.jar`: SAT solver (SAT4J).
- `VAL/Validate`: plan validator (Linux binary).
- `benchmarks/`: domains and problems.

## Compile

```bash
javac -d classes -cp "lib/*" src/fr/uga/pddl4j/examples/sat/*.java
```

## Run

```bash
java -cp "classes:lib/*" fr.uga.pddl4j.examples.sat.SAT benchmarks/gripper/domain.pddl benchmarks/gripper/p01.pddl -i
```

Options:

- `-i, --info`: display the fluents, actions, initial state and goal of the problem.
- `-t <seconds>`: timeout (default 600).
- `-V, --validator <path>`: path to VAL Validate (default `VAL/Validate`).
- `--no-validation`: do not check the plan with VAL.
- `-h`: help.

At the end, the plan found is checked with VAL: `VAL: the plan is VALID` or
`VAL: the plan is INVALID` (followed by the reason given by VAL).
