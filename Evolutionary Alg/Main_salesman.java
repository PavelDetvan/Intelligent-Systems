import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

// Class representing an individual TSP solution (a tour)
class TSPIndividual {
    // The tour is represented as a permutation of city indices.
    private List<Integer> tour;
    private Double fitness; // Fitness = 1 / totalDistance

    // Constructor: randomly generate a tour for a given number of cities.
    public TSPIndividual(int numCities, Random random) {
        tour = new ArrayList<>();
        for (int i = 0; i < numCities; i++) {
            tour.add(i);
        }
        Collections.shuffle(tour, random);
        fitness = null;
    }

    // Copy constructor from a given tour.
    public TSPIndividual(List<Integer> tour) {
        this.tour = new ArrayList<>(tour);
        this.fitness = null;
    }

    public List<Integer> getTour() {
        return tour;
    }

    public Double getFitness() {
        return fitness;
    }

    public void setFitness(Double fitness) {
        this.fitness = fitness;
    }

    // Swap mutation: swap two random cities in the tour.
    public void mutate(double mutationProbability, Random random) {
        if(random.nextDouble() < mutationProbability) {
            int i = random.nextInt(tour.size());
            int j = random.nextInt(tour.size());
            // Swap cities at index i and j.
            Collections.swap(tour, i, j);
            // Invalidate fitness (needs recalculation).
            fitness = null;
        }
    }
}

// Class representing the TSP Evolutionary Algorithm
class TSPEvolutionaryAlgorithm2 {
    private int populationSize;
    private int numCities;
    private double crossoverProbability;
    private double mutationProbability;
    private int generations;
    private List<TSPIndividual> population;
    private Random random;

    // Coordinates for cities (placed evenly on a unit circle)
    private double[] cityX;
    private double[] cityY;

    // Flag to choose selection method: true for tournament, false for roulette.
    private boolean useTournament = true; 

    public TSPEvolutionaryAlgorithm2(int populationSize, int numCities, double crossoverProbability, double mutationProbability, int generations) {
        this.populationSize = populationSize;
        this.numCities = numCities;
        this.crossoverProbability = crossoverProbability;
        this.mutationProbability = mutationProbability;
        this.generations = generations;
        this.random = new Random();
        initializeCities();  // Generate coordinates for cities.
        this.population = initializePopulation();
    }

    // Generate cities on a unit circle (evenly spaced)
    private void initializeCities() {
        cityX = new double[numCities];
        cityY = new double[numCities];
        double angleIncrement = 2 * Math.PI / numCities;
        for (int i = 0; i < numCities; i++) {
            double angle = i * angleIncrement;
            cityX[i] = Math.cos(angle);
            cityY[i] = Math.sin(angle);
        }
        System.out.println("Cities (on unit circle):");
        for (int i = 0; i < numCities; i++) {
            System.out.printf("City %d: (%.3f, %.3f)%n", i, cityX[i], cityY[i]);
        }
        System.out.println();
    }

    // Create initial population with random tours.
    private List<TSPIndividual> initializePopulation() {
        List<TSPIndividual> pop = new ArrayList<>();
        for (int i = 0; i < populationSize; i++) {
            pop.add(new TSPIndividual(numCities, random));
        }
        return pop;
    }

    // Compute the total distance of a tour.
    private double computeDistance(TSPIndividual individual) {
        List<Integer> tour = individual.getTour();
        double totalDist = 0.0;
        for (int i = 0; i < tour.size() - 1; i++) {
            int cityA = tour.get(i);
            int cityB = tour.get(i + 1);
            totalDist += distanceBetween(cityA, cityB);
        }
        // Add distance from last city back to first city.
        totalDist += distanceBetween(tour.get(tour.size() - 1), tour.get(0));
        return totalDist;
    }

    // Euclidean distance between two cities.
    private double distanceBetween(int cityA, int cityB) {
        double dx = cityX[cityA] - cityX[cityB];
        double dy = cityY[cityA] - cityY[cityB];
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Evaluate fitness for each individual. Fitness is 1 / totalDistance.
    private void evaluateFitness() {
        for (TSPIndividual individual : population) {
            if (individual.getFitness() == null) {
                double totalDistance = computeDistance(individual);
                // Avoid division by zero (should not happen with positive distances)
                individual.setFitness(1.0 / totalDistance);
            }
        }
    }

    // Tournament selection: choose the best individual from a random subset.
    private TSPIndividual tournamentSelection() {
        int tournamentSize = 5; // Adjust tournament size as needed.
        TSPIndividual best = null;
        for (int i = 0; i < tournamentSize; i++) {
            TSPIndividual contender = population.get(random.nextInt(populationSize));
            if (best == null || contender.getFitness() > best.getFitness()) {
                best = contender;
            }
        }
        return best;
    }

    // Roulette wheel selection: choose an individual with probability proportional to fitness.
    private TSPIndividual rouletteSelection() {
        double totalFitness = 0.0;
        for (TSPIndividual ind : population) {
            totalFitness += ind.getFitness();
        }
        double slice = random.nextDouble() * totalFitness;
        double sum = 0.0;
        for (TSPIndividual ind : population) {
            sum += ind.getFitness();
            if (sum >= slice) {
                return ind;
            }
        }
        // Fallback (should not happen)
        return population.get(population.size() - 1);
    }

    // Parent selection method: choose one individual using the specified method.
    private TSPIndividual selectParent() {
        if (useTournament) {
            return tournamentSelection();
        } else {
            return rouletteSelection();
        }
    }

    // Order Crossover (OX)
    private List<TSPIndividual> crossover(TSPIndividual parent1, TSPIndividual parent2) {
        // Choose two random cut points.
        int cut1 = random.nextInt(numCities);
        int cut2 = random.nextInt(numCities);
        if (cut1 > cut2) {
            int temp = cut1;
            cut1 = cut2;
            cut2 = temp;
        }
        // Create two children.
        List<Integer> child1 = new ArrayList<>(Collections.nCopies(numCities, -1));
        List<Integer> child2 = new ArrayList<>(Collections.nCopies(numCities, -1));
        
        // Copy the slice from parent1 to child1 and from parent2 to child2.
        for (int i = cut1; i <= cut2; i++) {
            child1.set(i, parent1.getTour().get(i));
            child2.set(i, parent2.getTour().get(i));
        }
        
        // Fill in the remaining positions for child1 using parent2's order.
        int currentPos = (cut2 + 1) % numCities;
        for (int i = 0; i < numCities; i++) {
            int index = (cut2 + 1 + i) % numCities;
            int candidate = parent2.getTour().get(index);
            if (!child1.contains(candidate)) {
                child1.set(currentPos, candidate);
                currentPos = (currentPos + 1) % numCities;
            }
        }
        
        // Fill in the remaining positions for child2 using parent1's order.
        currentPos = (cut2 + 1) % numCities;
        for (int i = 0; i < numCities; i++) {
            int index = (cut2 + 1 + i) % numCities;
            int candidate = parent1.getTour().get(index);
            if (!child2.contains(candidate)) {
                child2.set(currentPos, candidate);
                currentPos = (currentPos + 1) % numCities;
            }
        }
        
        List<TSPIndividual> children = new ArrayList<>();
        children.add(new TSPIndividual(child1));
        children.add(new TSPIndividual(child2));
        return children;
    }

    // Mutation: Swap mutation
    private void mutate(TSPIndividual individual) {
        individual.mutate(mutationProbability, random);
    }

    // Print details of the best individual.
    private void printBestIndividualInfo(int generation) {
        TSPIndividual best = population.get(0);
        double totalDistance = computeDistance(best);
        System.out.println("Generation " + generation + " Best Distance = " + totalDistance + " (Fitness = " + best.getFitness() + ")");
        System.out.print("Tour: ");
        for (Integer city : best.getTour()) {
            System.out.print(city + " ");
        }
        System.out.println();
        System.out.println("------------------------------");
    }

    // Main evolution loop.
    public void loop() {
        for (int generation = 0; generation < generations; generation++) {
            evaluateFitness();
            // Sort population by fitness (highest first).
            population.sort((a, b) -> Double.compare(b.getFitness(), a.getFitness()));

            // Print every 20 generations and at generation 0 and final generation.
            if (generation == 0 || generation % 20 == 0 || generation == generations - 1) {
                printBestIndividualInfo(generation);
            }

            // Build a breeding pool.
            List<TSPIndividual> breedingPool = new LinkedList<>();
            while (breedingPool.size() < populationSize) {
                breedingPool.add(selectParent());
            }

            // Create next generation.
            List<TSPIndividual> nextGeneration = new ArrayList<>();
            while (nextGeneration.size() < populationSize) {
                TSPIndividual parent1 = breedingPool.remove(0);
                TSPIndividual parent2 = breedingPool.remove(0);
                List<TSPIndividual> offspring;
                if (random.nextDouble() < crossoverProbability) {
                    offspring = crossover(parent1, parent2);
                } else {
                    // No crossover; copy parents.
                    offspring = new ArrayList<>();
                    offspring.add(new TSPIndividual(parent1.getTour()));
                    offspring.add(new TSPIndividual(parent2.getTour()));
                }
                for (TSPIndividual child : offspring) {
                    mutate(child);
                    nextGeneration.add(child);
                    if (nextGeneration.size() >= populationSize) {
                        break;
                    }
                }
            }
            population = nextGeneration;
        }
        evaluateFitness();
        population.sort((a, b) -> Double.compare(b.getFitness(), a.getFitness()));
        printBestIndividualInfo(generations);
    }
}

public class Main_salesman {
    public static void main(String[] args) {
        // You can adjust these parameters for testing.
        TSPEvolutionaryAlgorithm2 ea = new TSPEvolutionaryAlgorithm2(
            100,       // Population size
            10,        // Number of cities
            0.8,       // Crossover probability
            0.1,       // Mutation probability
            100        // Number of generations
        );
        ea.loop();
    }
}
