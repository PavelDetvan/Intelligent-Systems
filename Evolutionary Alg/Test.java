public class Test {
    // Runs a single test with given parameters.
    public static void runTest(String testName, int populationSize, int numItems, double crossoverProb, double mutationProb, int generations) {
        System.out.println("======== " + testName + " ========");
        EvolutionaryAlgorithm ea = new EvolutionaryAlgorithm(populationSize, numItems, crossoverProb, mutationProb, generations);
        ea.loop();
        Individual best = ea.getBestIndividual();
        
        // Compute total value and total weight for the best solution.
        double totalValue = 0;
        double totalWeight = 0;
        StringBuilder genotypeStr = new StringBuilder();
        for (Boolean gene : best.getGenotype()) {
            genotypeStr.append(gene ? "1" : "0");
        }
        for (int i = 0; i < numItems; i++) {
            if (best.getGenotype().get(i)) {
                totalValue += ea.getValues(i);
                totalWeight += ea.getWeights(i);
            }
        }
        
        System.out.println("Final Best Fitness: " + best.getFitness());
        System.out.println("Best Genotype: " + genotypeStr.toString());
        System.out.println("Total Value: " + totalValue);
        System.out.println("Total Weight: " + totalWeight + (totalWeight <= ea.getCapacity() ? " (Feasible)" : " (Infeasible)"));
        System.out.println();
        // Print fitness history for plotting
        System.out.println("Fitness History (Best Fitness per Generation):");
        for (Double fit : ea.getFitnessHistory()) {
            System.out.print(fit + ", ");
        }
        System.out.println("\n");
    }
    
    public static void main(String[] args) {
        // Test with smaller population so convergence is not immediate.
        runTest("Test A (Small Population)", 100, 20, 0.8, 0.1, 100);
        // Test with smaller population and larger number of items.
        runTest("Test B (Small Population, More Items)", 100, 100, 0.8, 0.1, 100);
        // Test with moderate population and more generations.
        runTest("Test C (Moderate Population)", 500, 40, 0.8, 0.1, 200);
        // Test with large population.
        runTest("Test D (Large Population)", 1000, 40, 0.8, 0.1, 200);
        // Test with different mutation probability.
        runTest("Test E (Higher Mutation)", 500, 40, 0.8, 0.2, 200);
        // Test with higher mutation and lower crossover probability.
        runTest("Test F (Higher Mutation and Lower Crossover Probability)", 500, 40, 0.4, 0.4, 300);
    }
}
