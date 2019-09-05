/**
 * Josh Improgo
 * Project 1
 * CUNY ID: 23659573
 * Professor Krishna Mahavadi CS212
 */

import java.util.ArrayList;
import java.util.Scanner;

public class Pr19573 {
    public static void main(String[] args){
        ArrayList<String> statistics;
        ArrayList<Integer> averageRateOfDeparture = new ArrayList<>(100);
        ArrayList<Integer> averageRateOfArrival = new ArrayList<>(100);
        ArrayList<Integer> averageWaitTimeForTakeoff = new ArrayList<>(100);
        ArrayList<Integer> averageWaitTimeForLanding = new ArrayList<>(100);
        ArrayList<Integer> averageRateOfRefusals = new ArrayList<>(100);
        int idleTime = 0;

        int s_null = 0, s_arriving = 1, s_departing = 2;
        int endTime;

        //landing list size
        int landingSizeLimit; //1

        //arrival rate and departure rate should not exceed 0
        double arrivalRate, departureRate; //0

        Scanner input = new Scanner(System.in);

        do {
            System.out.println("Enter end time (at least 1000: ");
            endTime = input.nextInt();
        } while (endTime < 1000);

        statistics = new ArrayList<>(endTime);

        System.out.println("Enter the landing list size (keep the size small).");
        landingSizeLimit = input.nextInt(); //keep size between 1-3?

        Runway runway = new Runway(landingSizeLimit);

        do {
            //Notify user arrival rate + departure rate should not exceed 1.0
            System.out.println("The sum of the arrival and departure rate should not exceed 1.0");

            //Have user enter arrival rate
                arrivalRate = input.nextDouble();
            arrivalRate = .5;

            //Have user enter departure rate
            System.out.println("Enter the departure rate: ");
                departureRate = input.nextDouble();

        } while (!validate(arrivalRate, departureRate)); //validate if arrival and departure rate do not exceed 1.0


        int numberOfArrivedPlanes = 0;
        int numberOfDepartedPlanes = 0;
        int numberOfRefusedPlanes = 0;

        //representation of flight number for the planes
        int planeNumber = 0;

        //principal loop
        for (int time = 0; time < endTime; time++) {
            int numInIntOfRefPlanes = 0;

            //keep track of the planes arriving and departing
            int arrivingPlanes = poisson(arrivalRate);
            int departingPlanes = poisson(departureRate);

            //identify if planes are idle if both arriving and departing planes is 0
            boolean idle = arrivingPlanes > 0 && departingPlanes > 0;

            //Begin data entry for statistics
            String dataEntry = "Time Interval: " + time + " | Arriving Plane(s): " + arrivingPlanes + " | Departing Plane(s): " + departingPlanes;

            //Label arrival list
            dataEntry += "\n\tList of Planes Awaiting Arrival:";

            //get the ArrayList<Plane> landing from runway
            ArrayList<Plane> landingList = runway.getLanding();

            //get size of the landing list
            int landingSize = landingList.size();

            //Check if arrivingPlanes is 0
            if (arrivingPlanes == 0) { //arrivingPlanes
                if (landingSize == 0)
                    dataEntry += "\n\t\t\t|N/A";
                else {
                    for (int p = 0; p < landingList.size(); p++) {
                        Plane plane = landingList.get(p);
                        plane.setStatus(s_null);
                        dataEntry += "\n\t\t\t|" + plane + " is awaiting arrival.";
                    }

                }
            } else { //if it is greater than 0


                //create number of planes that will be arriving
                for (int p = 0; p < arrivingPlanes; p++) {//arrivingPlanes
                    //set flight number
                    int flightNumber = planeNumber;//time*endTime + p;

                    //increment plane number to keep track of each plane
                    planeNumber++;

                    //create new Plane object
                    Plane plane = new Plane(flightNumber, time, s_null);

                    //add to data entry the plane number
                    dataEntry += "\n\t\t\t|"+ plane;//Plane #" + flightNumber;

                    //if the runway can land a plane (if the landing list is not full)
                    if (runway.canLand(plane)) {

                        //add the plane to the landing list
                        runway.addPlane(plane);

                        //add to data entry plane is added for arrival
                        dataEntry += " is added for arrival.";//\t\t\t\t\t\t\t";
                        numberOfArrivedPlanes++;
                    } else { //if the plane cannot land
                        //Excess arriving planes should be refused and directed to a different airport and statistics should be kept

                        //add to data entry that plane cannot land
                        dataEntry += " cannot land as there are excess planes. Plane is refused and directed to a different airport.";
                        //                        System.out.print(" cannot land as there are excess planes. Plane is refused and directed to a different airport.");

                        //refuse plane
                        plane.refuse();

                        numberOfRefusedPlanes++;
                        numInIntOfRefPlanes++;
                    }
                }
            }

            //label data entry of departure list
            dataEntry += "\n\tList of Planes Awaiting Departure: ";

            //if there are no departing planes
            ArrayList<Plane> takeoffList = runway.getTakeoff();
            if (takeoffList.size() == 0) {
                //set to N\\A
                dataEntry += "\n\t\t\t|N/A";
            } else { //if there are departing planes
                //enter list of planes on departure list
                for (int p = 0; p < takeoffList.size(); p++) {
                    Plane plane = takeoffList.get(p);
                    plane.setStatus(s_null);
                    dataEntry += "\n\t\t\t|" + plane + " is awaiting departure.";
                }
            }

            dataEntry += "\n\t\t----------";

            //add to data entry the planes currently arriving
            dataEntry += "\n\tPlanes arriving:";

            //if there aren't any planes arriving
            if (landingSize == 0)
                dataEntry += "\n\t\t\t|N/A";
            else { //if there are planes arriving
                //continue to let planes land until the size is 0
//                while (landingList.size() > 0) {
                //get the first plane of the landing list
                Plane lP = landingList.get(0);

                //set status of the plane
                lP.setStatus(s_arriving);

                int initialTime = lP.getClockStart();

                //land plane
                lP.land(time);

                int waitTime = lP.getClockStart() - initialTime;

                //add to data entry the Plane details
                dataEntry += "\n\t\t\t|" + lP + " and has waited " + waitTime + " time interval(s) to arrive.";

                averageWaitTimeForLanding.add(waitTime);
                //get plane off landing list
                runway.activityLand(time);
                runway.addTakeoff(lP);
            }


            dataEntry += "\n\tPlanes departing:";

            if (arrivingPlanes == 0) {
                if (departingPlanes != 0) {
                    //create planes for departing planes
                    for (int p = 0; p < departingPlanes; p++) {
                        if (takeoffList.size() == 0) {
                            dataEntry += "\n\t\t|N/A";
                            break;
                        }

                        Plane plane = runway.getTakeoff().get(0);

                        int initialTime = plane.getClockStart();

                        //allow plane to fly
                        plane.fly(time);


                        int waitTime = plane.getClockStart() - initialTime;
                        //add to data entry the plane details
                        dataEntry += "\n\t\t\t|" + plane + " and has waited " + waitTime + " time interval(s) to depart.";

                        averageWaitTimeForTakeoff.add(waitTime);

                        //take plane off takeoff list
                        runway.activityTakeoff(time);
                        numberOfDepartedPlanes++;

                    }
                } else {
                    dataEntry += "\n\t\t\t|N/A";
                }
            } else {
                dataEntry += "\n\t\t\t|N/A";
            }

            //check if the runway has been idle
            if (idle) {
                idleTime++;
                dataEntry += "\nNOTE: RUNWAY HAS BEEN IDLE";
            }

            //add the data entry to the statistics list
            statistics.add(dataEntry);
            averageRateOfArrival.add(/*numInIntOfArrPlanes*/arrivingPlanes);
            averageRateOfDeparture.add(/*numInIntOfDepPlanes*/departingPlanes);
            averageRateOfRefusals.add(numInIntOfRefPlanes);

        }

        //close scanner
        input.close();

        //shutdown runway
        System.out.println("Shutting down runway.");
        runway.shutdown();

        //print each data entry from the statistics
        for (String dataEntry : statistics) {
            System.out.println(dataEntry + "\n");
        }

        //log the amount of time the runway was idle
        int total = numberOfArrivedPlanes + numberOfRefusedPlanes;

        System.out.println("");
        System.out.println("Total number of planes: " + total +
                "\nNumber of arrived planes: " + numberOfArrivedPlanes + " or " + numberOfArrivedPlanes + "/" + total + " or " + (double)numberOfArrivedPlanes/total*100 + "%" +
                "\nNumber of departed planes: " + numberOfDepartedPlanes + " or " + numberOfDepartedPlanes + "/" + total + " or " + (double)numberOfDepartedPlanes/total*100 + "%" +
                "\nNumber of refused planes: " + numberOfRefusedPlanes + " or " + numberOfRefusedPlanes + "/" + total + " or " + (double)numberOfRefusedPlanes/total*100 + "%");

        System.out.println("Runway was idle for " + idleTime + "/" + endTime + " or " + (double)idleTime/endTime*100 + "%");

        System.out.println("Average rate of arrivals: " + getAverage(averageRateOfArrival) +
                "\nAverage rate of departures: " + getAverage(averageRateOfDeparture) +
                "\nAverage rate of refusals: " + getAverage(averageRateOfRefusals) +
                "\nAverage wait time for landing: " + getAverage(averageWaitTimeForLanding) +
                "\nAverage wait time for takeoff: " + getAverage(averageWaitTimeForTakeoff));
    }
//    }

    /**
     * Gets the average of an ArrayList
     * @param averageList an ArrayList<Integer> that will be averaged
     * @return the average of the data entered in the ArrayList
     */
    public static double getAverage(ArrayList<Integer> averageList) {
        int sum = 0;
        int size = averageList.size();

        for (int i = 0; i < size; i++)
            sum += averageList.get(i);

        return (double) sum/size;

    }
    /**
     * Determines if the runway is saturated
     * @param arrivalRate the rate of arrival of planes
     * @param departureRate the rate of departure of planes
     * @return a boolean that checks if the arrival rate and the departure rate is less than 1
     */
    public static boolean validate(double arrivalRate, double departureRate) {
        return arrivalRate + departureRate < 1.0;
    }
    /**
     * Uses the poisson statistical distribution to make calculations based on rate
     * @param rate a double that represents any rate
     * @return an int that uses the poisson statistical distribution
     */
    public static int poisson(double rate) {
        double limit = Math.exp(-rate);
        double product = Math.random();
        int count = 0;
        while (product > limit) {
            count++;
            product *= Math.random();
        }
        return count;
    }
}

class Plane {
    //Instance variables
    private int flightNumber;
    private int clockStart;
    private int status;
    /**
     * null = 0
     * arriving = 1
     * departing = 2
     */

    //Constructors

    /**
     * Default constructor
     */
    Plane() {
        flightNumber = -1;
        clockStart = 0;
        status = 0;
    }

    /**
     * A 3 parameter constructor
     * @param fltNumber an int that represents the flight number of a plane
     * @param time an int that represents the time increment of when it occurred an int that represents the time increment of when it occurred
     * @param status an int that represents the status of the Plane
     */
    Plane(int fltNumber, int time, int status) {
        flightNumber = fltNumber;
        clockStart = time;
        this.status = status;
    }

    //methods

    /**
     * If landing array list is max, the size of the plane is redirected to a different airport
     */
    void refuse() {
        status = 0;
    }

    /**
     * Arriving flight lands and once it lands, it will be removed from the list.
     * @param time an int that represents the time increment of when it occurred
     */
    void land(int time) {
        clockStart = time;
        status = 1;
    }

    /**
     * Departing flight takes off, and it is deleted from the takeoff list.
     * @param time an int that represents the time increment of when it occurred
     */
    void fly(int time) {
        clockStart = time;
        status = 2;
//            Runway
    }

    /**
     * returns the time at which a plane joined the appropriate Runway ArrayList<Plane>.
     * @return
     */
    int started() {
        return clockStart;
    }

    /**
     * Both arriving and departing planes must find how long they had to
     * wait in line before landing or departing and print that information.
     */

    /**
     * Accesses the flight number of the plane
     * @return an int that represents the flight number of the plane
     */
    public int getFlightNumber() {
        return flightNumber;
    }

    /**
     * Mutates the flight number of the plane
     * @param flightNumber an int that will change the flight number of the plane
     */
    public void setFlightNumber(int flightNumber) {
        this.flightNumber = flightNumber;
    }

    /**
     * Accesses the clock start of the plane
     * @return an int that represents the clock start of the plane
     */
    public int getClockStart() {
        return clockStart;
    }

    /**
     * Mutates the clock start of the plane
     * @param clockStart an int that will change the clock start of the plane
     */
    public void setClockStart(int clockStart) {
        this.clockStart = clockStart;
    }

    /**
     * Accesses the status of the plane
     * @return an int that represents the status of the plane
     */
    public int getStatus() {
        return status;
    }

    /**
     * Mutates the status of the plane
     * @param status an int that represents the status of the Plane
     */
    public void setStatus(int status) {
        this.status = status;
    }

    public String toString() {
        return "Plane #" + flightNumber + "\t[Status: " + status + "]\t\t[Clock Start: " + clockStart+ "]";
    }
}

/**
 * 4 class Runway
 */
class Runway {
    //Instance variables
    private ArrayList<Plane> landing;
    private ArrayList<Plane> takeoff;
    private int listLimit;

    //Constructors

    /**
     * One parameter constructor
     * @param limit an int that represents the limit of the number of planes in the runway
     */
    Runway(int limit) {
        landing = new ArrayList<>(limit);
        takeoff = new ArrayList<>(limit);
        listLimit = limit;
    }
    //methods

    /**
     * Determines if the plane can enter the landing list
     * @param current a Plane object that is currently being addressed
     * @return a boolean that determines whether a plane can land
     */
    boolean canLand(Plane current) {
            /*boolean land = landing.size() + 1 < listLimit;
            if (land)
                addPlane(current);*/
        return /*land;*/landing.size() + takeoff.size() < 2*listLimit;
    }

    /**
     * Adds the current plane to the landing list
     * @param current a Plane object that is currently being addressed
     */
    void addPlane(Plane current) {
        landing.add(current);
    }

    /**
     * Adds the current plane to the takeoff list
     * @param current a Plane object that is currently being addressed
     */
    void addTakeoff(Plane current) {
        takeoff.add(current);
    }

    /**
     * Let's the first plane in the list land and then removes it after the plane lands
     * @param time an int that represents the time increment of when it occurred
     */
    void activityLand(int time) {
        landing.get(0).land(time);
        landing.remove(0);
    }

    /**
     * Let's the first plane in the takeoff list take off
     * @param time an int that represents the time increment of when it occurred
     */
    void activityTakeoff(int time) {
        takeoff.get(0).fly(time);
        takeoff.remove(0);
    }

    /**
     * Let's the runway become idle if there is no plane to land or takeoff
     * @param time an int that represents the time increment of when it occurred
     */
    void runIdle(int time) {
        System.out.println("IDLE @: " + time);
    }

    /**
     * Shuts down the runway
     */
    void shutdown() {
        while (landing.size() > 0) {
            landing.remove(0);
        }
    }

    /**
     * Accesses the landing ArrayList of the runway
     * @return the ArrayList landing of the runway
     */
    public ArrayList<Plane> getLanding() {
        return landing;
    }

    /**
     * Mutates the landing ArrayList of the runway
     * @param landing an ArrayList which the landing list will be set to
     */
    public void setLanding(ArrayList<Plane> landing) {
        this.landing = landing;
    }

    /**
     * Accesses the takeoff ArrayList of the runway
     * @return the ArrayList takeoff of the runway
     */
    public ArrayList<Plane> getTakeoff() {
        return takeoff;
    }

    /**
     * Mutates the takeoff ArrayList of the runway
     * @param takeoff an ArrayList which the takeoff list will be set to
     */
    public void setTakeoff(ArrayList<Plane> takeoff) {
        this.takeoff = takeoff;
    }

    /**
     * Accesses the list limit of the runway
     * @return an int that represents the list limit
     */
    public int getListLimit() {
        return listLimit;
    }

    /**
     * Mutates the list limit of the runway
     * @param listLimit an int that the list limit will be set to
     */
    public void setListLimit(int listLimit) {
        this.listLimit = listLimit;
    }
}
