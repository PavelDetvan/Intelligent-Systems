import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

// ---------- TSP Evolutionary Algorithm Classes ----------

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
            Collections.swap(tour, i, j);
            fitness = null;
        }
    }
}

// Class representing the TSP Evolutionary Algorithm.
class TSPEvolutionaryAlgorithm {
    private int populationSize;
    private int numCities;
    private double crossoverProbability;
    private double mutationProbability;
    private int generations;
    private List<TSPIndividual> population;
    private Random random;

    // Coordinates for cities (randomly placed)
    double[] cityX;
    double[] cityY;
    // Coordinate range (0 to 1 in this example)
    private double xMin = 0, xMax = 1;
    private double yMin = 0, yMax = 1;

    // Flag to choose selection method: true for tournament, false for roulette.
    private boolean useTournament = true; 

    public TSPEvolutionaryAlgorithm(int populationSize, int numCities, double crossoverProbability, double mutationProbability, int generations) {
        this.populationSize = populationSize;
        this.numCities = numCities;
        this.crossoverProbability = crossoverProbability;
        this.mutationProbability = mutationProbability;
        this.generations = generations;
        this.random = new Random();
        initializeCities();  // Generate random coordinates for cities.
        this.population = initializePopulation();
    }

    // Generate cities at random positions.
    private void initializeCities() {
        cityX = new double[numCities];
        cityY = new double[numCities];
        for (int i = 0; i < numCities; i++) {
            cityX[i] = xMin + random.nextDouble() * (xMax - xMin);
            cityY[i] = yMin + random.nextDouble() * (yMax - yMin);
        }
        // Print city coordinates for reference.
        System.out.println("City Coordinates (random):");
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
    double computeDistance(TSPIndividual individual) {
        List<Integer> tour = individual.getTour();
        double totalDist = 0.0;
        for (int i = 0; i < tour.size() - 1; i++) {
            int cityA = tour.get(i);
            int cityB = tour.get(i + 1);
            totalDist += distanceBetween(cityA, cityB);
        }
        totalDist += distanceBetween(tour.get(tour.size() - 1), tour.get(0)); // Return to start.
        return totalDist;
    }

    // Euclidean distance between two cities.
    private double distanceBetween(int cityA, int cityB) {
        double dx = cityX[cityA] - cityX[cityB];
        double dy = cityY[cityA] - cityY[cityB];
        return Math.sqrt(dx * dx + dy * dy);
    }

    // Evaluate fitness for each individual. Fitness = 1 / totalDistance.
    private void evaluateFitness() {
        for (TSPIndividual individual : population) {
            if (individual.getFitness() == null) {
                double totalDistance = computeDistance(individual);
                individual.setFitness(1.0 / totalDistance);
            }
        }
    }

    // Tournament selection.
    private TSPIndividual tournamentSelection() {
        int tournamentSize = 5;
        TSPIndividual best = null;
        for (int i = 0; i < tournamentSize; i++) {
            TSPIndividual contender = population.get(random.nextInt(populationSize));
            if (best == null || contender.getFitness() > best.getFitness()) {
                best = contender;
            }
        }
        return best;
    }

    // Roulette wheel selection.
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
        return population.get(population.size() - 1);
    }

    // Choose parent using the selected method.
    private TSPIndividual selectParent() {
        if (useTournament) {
            return tournamentSelection();
        } else {
            return rouletteSelection();
        }
    }

    // Order Crossover (OX) operator.
    private List<TSPIndividual> crossover(TSPIndividual parent1, TSPIndividual parent2) {
        int cut1 = random.nextInt(numCities);
        int cut2 = random.nextInt(numCities);
        if (cut1 > cut2) {
            int temp = cut1;
            cut1 = cut2;
            cut2 = temp;
        }
        List<Integer> child1 = new ArrayList<>(Collections.nCopies(numCities, -1));
        List<Integer> child2 = new ArrayList<>(Collections.nCopies(numCities, -1));
        
        // Copy slice from parent1 to child1 and from parent2 to child2.
        for (int i = cut1; i <= cut2; i++) {
            child1.set(i, parent1.getTour().get(i));
            child2.set(i, parent2.getTour().get(i));
        }
        
        // Fill remaining positions for child1 using parent2's order.
        int currentPos = (cut2 + 1) % numCities;
        for (int i = 0; i < numCities; i++) {
            int index = (cut2 + 1 + i) % numCities;
            int candidate = parent2.getTour().get(index);
            if (!child1.contains(candidate)) {
                child1.set(currentPos, candidate);
                currentPos = (currentPos + 1) % numCities;
            }
        }
        
        // Fill remaining positions for child2 using parent1's order.
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

    // Mutation: Swap mutation.
    private void mutate(TSPIndividual individual) {
        individual.mutate(mutationProbability, random);
    }

    // Print details of the best individual.
    private void printBestIndividualInfo(int generation) {
        TSPIndividual best = population.get(0);
        double totalDistance = computeDistance(best);
        System.out.printf("Generation %d Best Distance = %.3f (Fitness = %.5f)%n", generation, totalDistance, best.getFitness());
        System.out.print("Tour: ");
        for (Integer city : best.getTour()) {
            System.out.print(city + " ");
        }
        System.out.println("\n------------------------------");
    }

    // Main evolution loop.
    public void loop() {
        for (int generation = 0; generation < generations; generation++) {
            evaluateFitness();
            Collections.sort(population, (a, b) -> Double.compare(b.getFitness(), a.getFitness()));
            if (generation == 0 || generation % 20 == 0 || generation == generations - 1) {
                printBestIndividualInfo(generation);
            }
            List<TSPIndividual> breedingPool = new LinkedList<>();
            while (breedingPool.size() < populationSize) {
                breedingPool.add(selectParent());
            }
            List<TSPIndividual> nextGeneration = new ArrayList<>();
            while (nextGeneration.size() < populationSize) {
                TSPIndividual parent1 = breedingPool.remove(0);
                TSPIndividual parent2 = breedingPool.remove(0);
                List<TSPIndividual> offspring;
                if (random.nextDouble() < crossoverProbability) {
                    offspring = crossover(parent1, parent2);
                } else {
                    offspring = new ArrayList<>();
                    offspring.add(new TSPIndividual(parent1.getTour()));
                    offspring.add(new TSPIndividual(parent2.getTour()));
                }
                for (TSPIndividual child : offspring) {
                    mutate(child);
                    nextGeneration.add(child);
                    if (nextGeneration.size() >= populationSize) break;
                }
            }
            population = nextGeneration;
        }
        evaluateFitness();
        Collections.sort(population, (a, b) -> Double.compare(b.getFitness(), a.getFitness()));
        printBestIndividualInfo(generations);
    }
    
    // Get the final population (for visualization)
    public List<TSPIndividual> getPopulation() {
        return population;
    }
    
    // Get the best individual (for visualization)
    public TSPIndividual getBestIndividual() {
        evaluateFitness();
        Collections.sort(population, (a, b) -> Double.compare(b.getFitness(), a.getFitness()));
        return population.get(0);
    }
}

// ---------- GUI Visualization Classes ----------

// This visualizer shows all tours in the final population (in light gray)
// and then highlights the best tour (in a thick blue line).
class TSPPopulationVisualizer extends JPanel {
    private double[] cityX;
    private double[] cityY;
    private List<TSPIndividual> population;
    private TSPIndividual bestIndividual;
    
    public TSPPopulationVisualizer(double[] cityX, double[] cityY, List<TSPIndividual> population, TSPIndividual bestIndividual) {
        this.cityX = cityX;
        this.cityY = cityY;
        this.population = population;
        this.bestIndividual = bestIndividual;
        setPreferredSize(new Dimension(600, 600));
        setBackground(Color.WHITE);
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D) g;
        int width = getWidth();
        int height = getHeight();
        
        // Draw all candidate tours in light gray.
        g2d.setColor(new Color(200, 200, 200, 100)); // Light gray with transparency.
        for (TSPIndividual individual : population) {
            List<Integer> tour = individual.getTour();
            for (int i = 0; i < tour.size(); i++) {
                int cityA = tour.get(i);
                int cityB = tour.get((i + 1) % tour.size());
                int x1 = (int) (cityX[cityA] * width);
                int y1 = (int) (cityY[cityA] * height);
                int x2 = (int) (cityX[cityB] * width);
                int y2 = (int) (cityY[cityB] * height);
                g2d.drawLine(x1, y1, x2, y2);
            }
        }
        
        // Draw the best tour in thick blue.
        g2d.setColor(Color.BLUE);
        g2d.setStroke(new BasicStroke(3));
        List<Integer> bestTour = bestIndividual.getTour();
        for (int i = 0; i < bestTour.size(); i++) {
            int cityA = bestTour.get(i);
            int cityB = bestTour.get((i + 1) % bestTour.size());
            int x1 = (int) (cityX[cityA] * width);
            int y1 = (int) (cityY[cityA] * height);
            int x2 = (int) (cityX[cityB] * width);
            int y2 = (int) (cityY[cityB] * height);
            g2d.drawLine(x1, y1, x2, y2);
        }
        g2d.setStroke(new BasicStroke(1));
        
        // Draw cities as red circles.
        int cityRadius = 6;
        for (int i = 0; i < cityX.length; i++) {
            int x = (int) (cityX[i] * width);
            int y = (int) (cityY[i] * height);
            g2d.setColor(Color.RED);
            g2d.fillOval(x - cityRadius, y - cityRadius, cityRadius * 2, cityRadius * 2);
            g2d.setColor(Color.BLACK);
            g2d.drawString(String.valueOf(i), x - 4, y - 8);
        }
        
        // Optionally, display the best tour's total distance.
        double bestDistance = 0.0;
        List<Integer> tour = bestIndividual.getTour();
        for (int i = 0; i < tour.size() - 1; i++) {
            int cityA = tour.get(i);
            int cityB = tour.get(i + 1);
            double dx = cityX[cityA] - cityX[cityB];
            double dy = cityY[cityA] - cityY[cityB];
            bestDistance += Math.sqrt(dx * dx + dy * dy);
        }
        int firstCity = tour.get(0);
        int lastCity = tour.get(tour.size()-1);
        double dx = cityX[firstCity] - cityX[lastCity];
        double dy = cityY[firstCity] - cityY[lastCity];
        bestDistance += Math.sqrt(dx * dx + dy * dy);
        g2d.setColor(Color.BLACK);
        g2d.drawString("Best Distance: " + String.format("%.3f", bestDistance), 10, 20);
    }
}

// ---------- Main Class ----------

public class Main_salesman_GUI {
    public static void main(String[] args) {
        TSPEvolutionaryAlgorithm ea = new TSPEvolutionaryAlgorithm(
            1000,    // Population size
            20,     // Number of cities
            0.8,    // Crossover probability
            0.15,    // Mutation probability
            2000     // Number of generations
        );
        ea.loop();
        List<TSPIndividual> finalPopulation = ea.getPopulation();
        TSPIndividual best = ea.getBestIndividual();
        System.out.printf("Final Best Tour Distance: %.3f%n", 1.0 / best.getFitness());
        
        // Create and display the GUI to visualize all candidate tours and highlight the best one.
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("TSP Population Visualization");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            TSPPopulationVisualizer visualizer = new TSPPopulationVisualizer(ea.cityX, ea.cityY, finalPopulation, best);
            frame.add(visualizer);
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
