
/*
 * Licensed under the Academic Free License (AFL 3.0).
 *     http://opensource.org/licenses/AFL-3.0
 *
 *  This code is distributed to CSULB students in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE, other than educational.
 *
 *  2018 Alvaro Monge <alvaro.monge@csulb.edu>
 *
 */

package csulb.cecs323.app;

import csulb.cecs323.model.*;
import org.apache.ibatis.jdbc.ScriptRunner;

import javax.persistence.*;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.Reader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Logger;

/**
 * A simple application to demonstrate how to persist an object in JPA.
 *
 * This is for demonstration and educational purposes only.
 */
public class JpaStarterApp {
    private EntityManager entityManager;

    private static final Logger LOGGER = Logger.getLogger(JpaStarterApp.class.getName());

    public JpaStarterApp(EntityManager manager) {
        this.entityManager = manager;
    }

    public static void main(String[] args) throws SQLException, FileNotFoundException {
        LOGGER.fine("Creating EntityManagerFactory and EntityManager");
        EntityManagerFactory factory = Persistence.createEntityManagerFactory("starter_PU");
        EntityManager manager = factory.createEntityManager();
        JpaStarterApp hw4application = new JpaStarterApp(manager);
        Scanner userScanner = new Scanner(System.in);




        // Any changes to the database need to be done within a transaction.
        // See: https://en.wikibooks.org/wiki/Java_Persistence/Transactions

        LOGGER.fine("Begin of Transaction");
        EntityTransaction tx = manager.getTransaction();

        tx.begin();
        DriverManager.registerDriver(new com.mysql.cj.jdbc.Driver());
        String mysqlUrl = "jdbc:mysql://10.33.5.165:3306/sec1group14?serverTimezone=UTC";

        System.out.println("Please enter your Username");
        String userName = userScanner.nextLine();
        System.out.println("Please enter your password");
        String passWord = userScanner.nextLine();
        Connection con = DriverManager.getConnection(mysqlUrl, userName, passWord);

        // Initialize the script runner
        ScriptRunner sr = new ScriptRunner(con);
        Reader reader = new BufferedReader(new FileReader("data.sql"));
        sr.runScript(reader);


        hw4application.mainMenu(tx);
//      hw4application.createAnimalEntity();
        tx.commit();
        LOGGER.fine("End of Transaction");

    }

    public void mainMenu(EntityTransaction tx) {

        Scanner scanner = new Scanner(System.in);
        int option  = 0;

        while (option != 6) {

            System.out.println("\n---------------------------------");
            System.out.println("[Main Menu]");
            System.out.println("1 - Query 1: Retrieve all shorthair cats with no vaccinations");
            System.out.println("2 - Query 2: Retrieve number of volunteers at Carson Animal Shelter");
            System.out.println("3 - Query 3: Retrieve people who have made a donation greater than the average amount");
            System.out.println("4 - Add an animal to a shelter");
            System.out.println("5 - Remove an animal (parent entity)");
            System.out.println("6 - Exit and Commit Changes");
            System.out.println("---------------------------------\n");

            System.out.print("Your choice: ");
            option = scanner.nextInt();

            if (option == 1) {

                System.out.println("Query 1: Retrieve all shorthair cats with no vaccinations");
                query1();
            } else if (option == 2) {

                System.out.println("Query 2: Retrieve people who have made a donation greater than the average amount");
                query2();
            } else if (option == 3) {

                System.out.println("Query 3: Retrieve number of volunteers at Carson Animal Shelter");
                query3();
            } else if (option == 4) {

                System.out.println("Add an animal to a shelter.");
                insertEntity();
            } else if (option == 5) {

                System.out.println("Choose an animal ID to remove, 17, 18, or 19");
                //         deleteEntity( "Puck");
                option = scanner.nextInt();
                deleteEntity(option);
            } else if (option == 6) {

                System.out.println("Exit Program");
                //         break;
            } else {

                System.out.println("End Program");
                //         break;
            }
            tx.commit();
        }
    }


    public void createAnimalEntity(){

        LOGGER.fine("Create Animal Object");
        //Animal test = new Animal( "rex", BEIGE, Animal.Sex.MALE, 14, FALSE);
        LOGGER.fine("Persisting Animal Goldie to DB");
        LOGGER.fine("Persisting Animal Sherlock to DB");
        //this.entityManager.persist(test);
    }

    public void query1() {

        //Query #1 in project-queries.sql

        String nativeQuery = "SELECT animal_id AS 'Animal ID', a.name AS 'Animal Name', breed_name AS 'Breed Type', s.name AS 'Shelter Name'\n" +
                "FROM animals a INNER JOIN shelters s ON a.SHELTER_shelter_id = s.shelter_id\n" +
                "               INNER JOIN breeds b ON a.BREED_breed_id = b.breed_id\n" +
                "               INNER JOIN zip_location zl ON s.ZIPLOCATION_zip_code = zl.zip_code\n" +
                "WHERE breed_name = 'SHORTHAIR' AND  a.name IN (\n" +
                "    SELECT name\n" +
                "        FROM animals a LEFT OUTER JOIN vaccinations v ON a.animal_id = v.ANIMAL_animal_id\n" +
                "        WHERE v.vacc_name IS NULL)\n" +
                "ORDER BY `Animal Name`";
        Query query = this.entityManager.createNativeQuery(nativeQuery);
        List<Object[]> queryList = query.getResultList();

        System.out.println("\nList the id, name, breed type, and shelter of animals of 'shorthair' in any of the shelters who do not have vaccines.");
        System.out.println("\n[Shorthaired Cats with no Vaccinations]");

        for (Object[] q1: queryList){

            System.out.println("\nAnimal Name: " + q1[1]);
            System.out.println("Animal ID: " + q1[0] + "\nBreed Type: " + q1[2] + "\nShelter Name: " + q1[3]);
            System.out.println();
        }
    }

    public void query2(){

        //Query #4 in project-queries.sql

        String nativeQuery = "SELECT p.first_name AS 'Donator First Name', p.last_name AS 'Last Name',\n" +
                "       p.street_addr AS 'Street Address', zl.city AS 'City', zl.state AS 'State', " +
                "       p.ZIPLOCATION_zip_code AS 'Zip Code', p2.amount AS 'Donation Amount'\n" +
                "FROM persons p INNER JOIN payments p2 ON p.person_id = p2.PERSON_person_id\n" +
                "               INNER JOIN shelters s ON p2.SHELTER_shelter_id = s.shelter_id\n" +
                "               INNER JOIN zip_location zl ON p.ZIPLOCATION_zip_code = zl.zip_code\n" +
                "WHERE amount >= ALL (\n" +
                "    SELECT AVG(amount)\n" +
                "    FROM payments p)";
        Query query = this.entityManager.createNativeQuery(nativeQuery);
        List<Object[]> queryList = query.getResultList();

        System.out.println("\nList the first name, last name, address, and donation amount of anyone who has made a " +
                "donation greater than the average donation amount. List the name of the shelter the " +
                "donation was made at");
        System.out.println("\n[Donators Above Average Donation Amount]");

        for (Object[] q: queryList){

            System.out.println("\nFirst Name: " + q[0]);
            System.out.println("Last Name: " + q[1]);
            System.out.println("Street Address: " + q[2]);
            System.out.println("City: " + q[3]);
            System.out.println("State: " + q[4]);
            System.out.println("Zip Code: " + q[5]);
            System.out.println("Donation Amount: $" + q[6]);

        }
    }

    public void query3(){

        //Query #2 in project-queries.sql

        String nativeQuery = "SELECT s.name AS 'Shelter Name', COUNT(P.person_id) AS '# of volunteers'\n" +
                "FROM shelters s INNER JOIN staffs s2 ON s.shelter_id = s2.SHELTER_shelter_id\n" +
                "                INNER JOIN zip_location zl ON s.ZIPLOCATION_zip_code = zl.zip_code\n" +
                "                INNER JOIN persons p ON s2.person_id = p.person_id\n" +
                "WHERE isVolunteer = TRUE AND s.name = 'Carson Animal Shelter'";
        Query query = this.entityManager.createNativeQuery(nativeQuery);
        List<Object[]> queryList = query.getResultList();

        System.out.println("\nList the animal shelters that have more than 1 volunteer. List the number of volunteers " +
                "each shelter has.");
        System.out.println("\n[Volunteers Across Shelters]");

        for (Object[] q: queryList) {

            System.out.println("\nShelter Name: " + q[0]);
            System.out.println("Number of Volunteers: " + q[1]);
        }
    }


    public Date formatDate(String myDate) throws ParseException {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Date convertDate = simpleDateFormat.parse(myDate);
        String date = simpleDateFormat.format(convertDate);
        return convertDate;
    }

    public void insertEntity(){

        String myName;
        Date myDob = new Date();
        boolean myFertile;
        Color myColor;
        Sex mySex;
        Breed myBreed;
        Shelter myShelter;

        Scanner scanner = new Scanner(System.in);

        System.out.println("Inserting a new entity animal into a shelter");

        System.out.print("\nEnter animal name: ");
        myName = scanner.nextLine();

        System.out.print("Enter Date of Birth (yyyy-MM-dd): ");
        String date = scanner.nextLine();
        try {
            myDob = formatDate(date);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        System.out.print("Enter fertile (True/False): ");
        myFertile = scanner.nextBoolean();

        System.out.println("Select Breed");
        System.out.println("Options for Breed: ");
        String breedNativeQuery = "SELECT b.* FROM breeds b";
        Query allBreedQuery = this.entityManager.createNativeQuery(breedNativeQuery, Breed.class);
        List<Breed> breedList = allBreedQuery.getResultList();

        for (Breed breed: breedList){

            System.out.println(breed.toString());
        }

        System.out.print("Choose a Breed ID: (from 1 to 24)");

        int breedChoice = scanner.nextInt();
        breedChoice--;

        System.out.println("Select Sex ");
        System.out.println("Options for Sex: ");

        String sexNativeQuery = "SELECT s.* FROM sex s";
        Query sexQuery = this.entityManager.createNativeQuery(sexNativeQuery, Sex.class);
        List<Sex> sexList = sexQuery.getResultList();

        for (Sex sex: sexList){

            System.out.println(sexList.indexOf(sex) + ") " +  sex.toString());
        }

        System.out.print("Choose sex (Pick #)");
        int sexChoice = scanner.nextInt();

        System.out.println("Select Color ");
        System.out.println("Options for Colors: ");

        String colorNativeQuery = "SELECT c.* FROM colors c";
        Query colorQuery = this.entityManager.createNativeQuery(colorNativeQuery, Color.class);
        List<Color> colorsList = colorQuery.getResultList();

        for (Color color: colorsList){

            System.out.println(colorsList.indexOf(color)+ ") " + color.toString());
        }

        System.out.print("Choose color: (Pick #)");
        int colorChoice = scanner.nextInt();

        System.out.print("Select Shelter: ");
        System.out.println("Options for Shelters: ");
        String shelterNativeQuery = "SELECT sh.* FROM shelters sh";
        Query shelterQuery = this.entityManager.createNativeQuery(shelterNativeQuery, Shelter.class);
        List<Shelter> shelterList = shelterQuery.getResultList();

        for (Shelter shelter: shelterList){

            System.out.println(shelterList.indexOf(shelter) + ") " + shelter.toString());
        }

        System.out.print("Choose shelter (Pick #): ");
        int shelterChoice = scanner.nextInt();

        myBreed = breedList.get(breedChoice);
        System.out.println("You selected: " + breedChoice + " Name: " + myBreed.getName());

        mySex = sexList.get(sexChoice);
        myColor = colorsList.get(colorChoice);
        myShelter = shelterList.get(shelterChoice);

        Animal myAnimal = new Animal( myName, myColor, mySex, myDob, myFertile);
        myAnimal.setBreed(myBreed);
        myAnimal.setShelter(myShelter);
        System.out.println("New animal successfully added: " + myAnimal.toString());

        LOGGER.fine("Persisting new Animal to DB");
        entityManager.persist(myAnimal);
        myAnimal.toString();

        String obtainedDate = "2020-12-11";
        Date date1 = new Date();
        try {
            date1 = formatDate(obtainedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        IntakeForm myForm = new IntakeForm(date1, "picked up");
        myForm.setAnimal(myAnimal);
        myForm.setShelter(myShelter);
        entityManager.persist(myForm);
    }

    public void deleteEntity(long n){

        System.out.println("Removing entity from vaccination which also deletes animal with animal id = " + n);

        Animal animal = this.entityManager.find(Animal.class, n);
        for (Vaccination vac: animal.getVaccinations()){
            this.entityManager.remove(vac);
        }
        this.entityManager.remove(animal);
        this.entityManager.flush();

    }

}

