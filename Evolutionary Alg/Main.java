import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;


// Class representing an individual candidate solution for the knapsack problem
// The genotype is a bitstring (List<Boolean>), where each bit indicates whether the corresponding item is included
class Individual {
    private List<Boolean> genotype;
    private Double fitness;

    // Constructor: Creates an individual with a random bitstring of given length
    public Individual(int genotypeLength) {
        // Initialize genotype 
        // For now, this is a bitstring that
        // gets filled with random Boolean values. 
        // Adapt to your liking 
        this.genotype = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < genotypeLength; i++) {
            this.genotype.add(random.nextBoolean());
        }
        this.fitness = null; // Gets computed later, need null value to indicate it's not computed yet
    }

    public Individual(List<Boolean> genotype) {
        // Initialize genotype with a given list of Boolean values
        // This is useful for creating offspring from parents
        // Again, adapt to your liking
        this.genotype = new ArrayList<>(genotype);
        this.fitness = null;
    }

    // Getter for genotype
    public List<Boolean> getGenotype() {
        return genotype;
    }

    // Getter and setter for fitness
    public Double getFitness() {
        return fitness;
    }
    public void setFitness(Double fitness) {
        this.fitness = fitness;
    }

    // Mutation method: Iterates through the genotype and flips each bit with a specified mutation probability
    public void mutate(double mutationProbability) {
        Random random = new Random();
        for (int i = 0; i < genotype.size(); i++) {
            if (random.nextDouble() < mutationProbability) {
                // Mutate by changing the value, for bitstrings this is flipping the bit
                // Make sure it fits your chosen representation
                genotype.set(i, !genotype.get(i));
            }
        }
    }
}

// Class implementing the evolutionary algorithm for the knapsack problem
class EvolutionaryAlgorithm {
    private int populationSize;
    private int genotypeLength;
    private double crossoverProbability;
    private double mutationProbability;
    private int generations;
    private List<Individual> population;
    private Random random;

    // Fields specific to the knapsack problem:
    private int[] weights; //holds the randomly generated weights for each item
    private int[] values; //holds the benefit (or reward) for each item
    private int capacity; //the maximum weight allowed in the knapsack
    
    public EvolutionaryAlgorithm(int populationSize, int genotypeLength, double crossoverProbability, double mutationProbability, int generations) {
        // Initialize the evolutionary algorithm with the given parameters
        this.populationSize = populationSize;
        this.genotypeLength = genotypeLength;
        this.crossoverProbability = crossoverProbability;
        this.mutationProbability = mutationProbability;
        this.generations = generations;
        this.random = new Random();
        // Initialize items: assign values 1, 2, 3, ... and random weights between 1 and 10.
        values = new int[genotypeLength];
        weights = new int[genotypeLength];
        int totalWeight = 0;
        for (int i = 0; i < genotypeLength; i++) {
            values[i] = i + 1;  // value of item i is i+1
            weights[i] = random.nextInt(10) + 1;  // weight between 1 and 10
            totalWeight += weights[i];
        }
        // Set the knapsack capacity, e.g., half the total weight of all items.
        capacity = totalWeight / 2;

        // Print item information and capacity for reference.
        System.out.println("Knapsack Capacity: " + capacity);
        System.out.print("Item values: ");
        for (int val : values) {
            System.out.print(val + " ");
        }
        System.out.println();
        System.out.print("Item weights: ");
        for (int wt : weights) {
            System.out.print(wt + " ");
        }
        System.out.println("\n");

        // Create the initial population.
        this.population = initializePopulation();
    }

    private List<Individual> initializePopulation() {
        // This creates a population of Individuals
        List<Individual> initialPopulation = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            // The specifics of the generated individual can be changed in the Individual class
            initialPopulation.add(new Individual(genotypeLength));
        }
        return initialPopulation;
    }

    // For each individual, sum the values and weights of selected items
    // If the total weight is within the capacity, the fitness equals the total value
    // Otherwise, the fitness is penalized by scaling the total value by (capacity/totalWeight)
    private void evaluateFitness() {
        for (Individual individual : population) {
            // Since fitness is set to null on construction, you can avoid computing it more than once
            if (individual.getFitness() == null) {
                // Placeholder: Calculate and set fitness for each individual
                // You can do the calculation here or in the Individual class
                // Use individual.setFitness(...) if you calculate it here
                // For now this just sets a random fitness
                double totalValue = 0;
                double totalWeight = 0;
                List<Boolean> genotype = individual.getGenotype();
                // Sum up the value and weight for items that are selected.
                for (int i = 0; i < genotype.size(); i++) {
                    if (genotype.get(i)) {
                        totalValue += values[i];
                        totalWeight += weights[i];
                    }
                }
                // If the total weight is within the capacity, fitness equals the total value.
                // Otherwise, apply a penalty by scaling down the value.
                if (totalWeight <= capacity) {
                    individual.setFitness(totalValue);
                } else {
                    individual.setFitness(totalValue * ((double) capacity / totalWeight));
                }
            }    
        }
    }

    // Parent selection using tournament selection
    // A subset of individuals (tournamentSize) is sampled randomly and the one with the highest fitness is selected
    private Individual selectBreeder() {
        // Placeholder: Implement parent selection logic (e.g., tournament or roulette wheel)
        // If you want you can choose to return multiple (e.g. 2) individuals, 
        // but you will have to change the singature of this method and change 
        // how it is used in the loop method
        // For now, this just returns a random individual
        int tournamentSize = 40;
        Individual best = null;
        // Randomly pick 'tournamentSize' individuals and select the one with highest fitness
        for (int i = 0; i < tournamentSize; i++) {
            Individual contender = population.get(random.nextInt(populationSize));
            if (best == null || contender.getFitness() > best.getFitness()) {
                best = contender;
            }
        }
        return best;
    }

    // Single-point crossover operator
    // A crossover point is chosen randomly; the offspring take the first part of one parent's genotype
    // and the second part from the other parent
    private List<Individual> crossover(Individual parent1, Individual parent2) {
        // This just implements single-point crossover. You can and should implement other types of crossover
        int crossoverPoint = random.nextInt(genotypeLength - 1) + 1;
        List<Boolean> child1Genotype = new ArrayList<>();
        List<Boolean> child2Genotype = new ArrayList<>();

        child1Genotype.addAll(parent1.getGenotype().subList(0, crossoverPoint));
        child1Genotype.addAll(parent2.getGenotype().subList(crossoverPoint, genotypeLength));

        child2Genotype.addAll(parent2.getGenotype().subList(0, crossoverPoint));
        child2Genotype.addAll(parent1.getGenotype().subList(crossoverPoint, genotypeLength));

        List<Individual> children = new ArrayList<>();
        children.add(new Individual(child1Genotype));
        children.add(new Individual(child2Genotype));
        return children;
    }

    // Print detailed info about the best individual.
    private void printBestIndividualInfo(int generation) {
        Individual best = population.get(0);
        double totalValue = 0;
        double totalWeight = 0;
        StringBuilder genotypeStr = new StringBuilder();
        List<Boolean> genotype = best.getGenotype();
        for (int i = 0; i < genotype.size(); i++) {
            boolean gene = genotype.get(i);
            genotypeStr.append(gene ? "1" : "0");
            if (gene) {
                totalValue += values[i];
                totalWeight += weights[i];
            }
        }
        System.out.println("Generation " + generation + " Best Fitness = " + best.getFitness());
        System.out.println("Best genotype: " + genotypeStr.toString());
        System.out.println("Total Value: " + totalValue);
        System.out.println("Total Weight: " + totalWeight + (totalWeight <= capacity ? " (Feasible)" : " (Infeasible)"));
        System.out.println("------------------------------");
    }

    public void loop() {
        for (int generation = 0; generation < generations; generation++) {
            evaluateFitness();

            // Sort population by fitness in descending order
            population.sort((a, b) -> Double.compare(b.getFitness(), a.getFitness()));
            // Print the best fitness in the current generation
            if (generation == 0) 
                System.out.println("Generation Zero (Probably Random Init): Best Fitness = " + population.get(0).getFitness());
            else 
                System.out.println("Generation " + (generation) + ": Best Fitness = " + population.get(0).getFitness());


            // Generate breeding pool
            List<Individual> breedingPool = new LinkedList<>();
            while (breedingPool.size() < populationSize)
                breedingPool.add(selectBreeder());

            List<Individual> nextGeneration = new ArrayList<>();

            // If you want to use Elitism, this is the place.
            // You can add the best individuals from the current generation to the next generation
            // You might want to avoid mutation on these individuals below

            // This loop creates the (rest of the) next generation
            while (nextGeneration.size() < populationSize) {
                Individual parent1 = breedingPool.remove(0);
                Individual parent2 = breedingPool.remove(0);

                List<Individual> offspring;
                // Crossover with probability crossoverProbability
                if (random.nextDouble() < crossoverProbability)
                    offspring = crossover(parent1, parent2);
                else {
                    // If no crossover, offspring are the parents
                    offspring = new ArrayList<>();
                    offspring.add(parent1);
                    offspring.add(parent2);
                }

                for (Individual child : offspring) {
                    // Mutate with probability mutationProbability
                    child.mutate(mutationProbability);
                    // Then add the child to the next generation
                    nextGeneration.add(child);
                    if (nextGeneration.size() >= populationSize) break; // Odd population size catch
                }
            }

            // Replace the old population with the new generation
            population = nextGeneration;
        }
        // Evaluate the fitness of the final generation
        evaluateFitness();
        // Sort population by fitness in descending order one last time
        population.sort((a, b) -> Double.compare(b.getFitness(), a.getFitness()));
        // Print the best fitness in the final generation
        // You can also print the best individual's genotype here
       // System.out.println("Final Generation, i.e. " + (generations) + ": Best Fitness = " + population.get(0).getFitness());
       printBestIndividualInfo(generations);
    }
}

public class Main {
    public static void main(String[] args) {
        // Initialize the evolutionary algorithm
        EvolutionaryAlgorithm ea = new EvolutionaryAlgorithm(
                1000,         // Population size
                10,          // Genotype length
                0.8,    // Crossover probability
                0.1,        // Mutation probability
                300          // Number of generations
        );

        // Run the evolution (implement fitness evaluation and selection to make this functional)
        ea.loop();
    }
}