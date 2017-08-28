package gc;

import kn.uni.voronoitreemap.j2d.Point2D;

/*
    Contains and manipulates all chromosome data
 */

public class Chromosome {

    static int maxSize; // max size of initial chromosome

    private StringBuffer chromo; // encoded chromosome in binary
    double fitness; // fitness score

    // initializes chromosome with random size and coordinates
    Chromosome() {
        chromo = new StringBuffer(Main.geneLength * 3);

        int size = GlobalVars.rand.nextInt(maxSize);
        addGene(size); // first gene is size of circle
        addGene(GlobalVars.rand.nextInt(GlobalVars.screenWidth - size)); // second gene is center x-coord of circle
        addGene(GlobalVars.rand.nextInt(GlobalVars.screenHeight - size)); // third gene is center y-coord of circle

        // calculates initial fitness
        calcFitness();
    }

    // adds gene to the chromosome, encoded in binary
    private void addGene(int i) {
        String gene = Integer.toBinaryString(i);
        for (int x = gene.length(); x < Main.geneLength; x++)
            chromo.append('0');
        chromo.append(gene);
    }

    // decodes the chromosome binary to understandable format using numbers
    String decode() {
        String decodedChromo = "";

        for (int x = 0; x < chromo.length(); x += Main.geneLength)
            decodedChromo += Integer.parseInt(chromo.substring(x, x + Main.geneLength), 2) + (x != chromo.length() - Main.geneLength ? "-" : "");

        return decodedChromo;
    }

    // returns new circle object with the data provided by the chromosome in this chromosome object
    CircleData getCircleData() {
        String[] genes = decode().split("-");
        return new CircleData(Integer.valueOf(genes[0]), new Point2D(Integer.valueOf(genes[1]),  Integer.valueOf(genes[2])));
    }

    // calculates fitness of circle
    void calcFitness() {
        /*
            If chromosome generates a circle that fits the specifications provided by proximityToMax variable, which is used
            to determine how close to the max circle specified by the voronoi diagram the circle has to get to be considered
            a valid solution
            If chromosome meets specifications, it is assigned the maximum value possible, otherwise it is assigned an inverse score
            to how close it is to the max value
            If the circle is deemed not valid because it is out of bounds or collides with static circles it is not discarded,
            but fitness score is decreased
         */
        String[] genes = decode().split("-");
        if (Integer.valueOf(genes[0]) >= GlobalVars.largestRadius * GlobalVars.proximityToMax)
            fitness = Integer.MAX_VALUE;
        else
            fitness = 1 / Math.abs(GlobalVars.largestRadius - (Integer.valueOf(genes[0])));

        if (!Main.isValid(getCircleData()))
            fitness /= 5;

    }

    // swaps bits from chromosomes from a random position, either forward or backwards
    void crossover(Chromosome c) {
        if (GlobalVars.rand.nextDouble() > GlobalVars.crossoverRate)
            return;

        int pos = GlobalVars.rand.nextInt(chromo.length());

        if (GlobalVars.rand.nextBoolean())
            for (int x = pos; x < chromo.length(); x++) {
                char tmp = c.chromo.charAt(x);
                c.chromo.setCharAt(x, this.chromo.charAt(x));
                this.chromo.setCharAt(x, tmp);
            }
        else
            for (int x = pos; x > 0; x--) {
                char tmp = c.chromo.charAt(x);
                c.chromo.setCharAt(x, this.chromo.charAt(x));
                this.chromo.setCharAt(x, tmp);
            }
    }

    // mutates a random bit in the chromosome
    void mutate() {
        if (GlobalVars.rand.nextDouble() > GlobalVars.mutationRate)
            return;

        int pos = GlobalVars.rand.nextInt(chromo.length() - 1);
        chromo.setCharAt(pos, chromo.charAt(pos) == '0' ? '1' : '0');
    }
}
