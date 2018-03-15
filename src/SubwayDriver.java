
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 * Driver class for project 5.
 *
 * If you do the extra credit, and can support deletion, invoke the driver with
 * flag "--extra-credit" as the first command-line argument.  Otherwise, we
 * assume deletion is not supported.
 *
 * This reads commands from standard input.  The commands are as follows:
 * 
 * "input x y s"
 * ... where x and y are arbitrary int coordinates, and s is a string
 * strictly delimited by whitespace (no way to quote it or escape it).
 * This command creates a graph vertex in the graph at coordinates (x,y)
 * and associates string s with it.  In this project we assume it represents
 * a subway station, and s represents the name of the station, not necessarily
 * unique.  If there is already a station at coordinates (x,y) then this
 * command updates the name string.  The command produces no output.
 *
 * "Adjacent x1 y1 x2 y2"
 * . . . where x1, y1, x2, and y2 are arbitrary int coordinates.
 * If there is a station at coordinates (x1, y1) and also a station at
 * coordinates (x2, y2), then this command creates an undirected graph
 * edge between the vertices, regardless of any other geometric concerns.
 * The edge represents a subway tunnel directly connecting the stations.
 * If either station does not exist, the command has no effect.
 * The command produces no output.
 *
 * "Route x1 y1 x2 y2"
 * . . . where x1, y1, x2, and y2 are arbitrary int coordinates.
 * If there is a station at coordinates (x1, y1) and also a station at
 * coordinates (x2, y2), then this command computes a shortest path
 * (i.e., minimum number of stations) between stations.
 * If the start or end station does not exist, or the stations are not
 * connected in the graph, then the command prints "NO PATH"; otherwise
 * the output consists of lines showing the stations along a path
 * in the graph.
 *
 * "Delete x y"
 * This command removes a vertex at (x,y) and its associated edges from
 * the graph.  If there is no station at coordinates (x, y), the command
 * is ignored.  The command produces no output.
 * For this assignment, this command is optional (for extra credit) --
 * see the assignment for details.
 *
 * Example
 *
 *	INPUT ===============

insert 1 1 red
insert 1 2 orange
insert 2 1 green
insert 2 2 blue
Adjacent 1 1 1 2
Adjacent 1 1 2 1
Adjacent 1 1 2 2
Adjacent 1 2 2 2
Adjacent 2 1 2 2
Route 1 2 2 1

 *	OUTPUT ===============

	 Station 1, 2 orange
	 Station 1, 1 red
	 Station 2, 1 green

 * Version 1.0
 */
public class SubwayDriver {

	// State machine states
	static final int RESET = 0, INSERT = 1, ADJACENT = 2, ROUTE = 3, DELETE = 4;

	// Input sequence states
	static final int X1IN = 0, Y1IN = 1, X2IN = 2, Y2IN = 3, STRINGYSTUFF = 4;


	// Print an error message and exit.
	public static void exit(String message) {
		System.err.println("Error: " + message);
		System.exit(1);
	}


	// Look for extra-credit flag and launch the state machine based on it.
	public static void main(String[] argv)
		throws FileNotFoundException, IOException
	{
		boolean ec = argv.length > 1 && argv[1].equals("--extra-credit");

		// Read from standard input
		driver(new BufferedReader(new InputStreamReader(System.in)), ec);
	}


	// Tedious error handling.  If the state is bad, NO RETURN, just exit.
	private static void check_for_bad_state(
		int cmd_state, int data_seq, boolean does_del)
	{
		if (RESET == cmd_state)
			return; // guaranteed fine

		if (cmd_state < INSERT || DELETE < cmd_state)
			exit("internal error 1:  bad state " + cmd_state);
		if (DELETE == cmd_state && ! does_del)
			exit("deletion unsupported (lacking --extra-credit flag)");

		if (STRINGYSTUFF == data_seq) {
		   	if (cmd_state != INSERT)
				exit("internal error 2:  STRINGYSTUFF without INSERT");
		} else if (data_seq < X1IN || Y2IN < data_seq)
			exit("internal error 3:  bad data sequence " + data_seq);
		else if (X2IN <= data_seq && INSERT == cmd_state)
			exit("internal error 4:  INSERT post Y1IN");
		else if (X2IN <= data_seq && DELETE == cmd_state)
			exit("internal error 5:  DELETE post Y1IN");
	}


	// Driver state machine for the subway class.
	private static void driver(BufferedReader br, boolean does_deletion)
		throws FileNotFoundException, IOException
	{
		/*
		 * Instantiate the graph data structure.
		 */
		SubwayGraph subway = new SubwayGraph();

		// Store state-machine state, and buffer for coordinates
		int cmd_state = RESET, data_seq = X1IN, x1 = 0, y1 = 0, x2 = 0;

		// Loop through the lines of input, and divide each line at whitespace:
		for (String line = br.readLine(); line != null; line = br.readLine())
			for (StringTokenizer tz = new StringTokenizer(line);
					tz.hasMoreTokens(); ) {

				final String token = tz.nextToken();

				check_for_bad_state(cmd_state, data_seq, does_deletion);

				if (RESET == cmd_state) {
					data_seq = X1IN; // reset input sequence also

					if (0 == token.compareTo("insert"))
						cmd_state = INSERT;
					else if (0 == token.compareTo("Adjacent"))
						cmd_state = ADJACENT;
					else if (0 == token.compareTo("Route"))
						cmd_state = ROUTE;
					else if (0 == token.compareTo("Delete"))
						cmd_state = DELETE;
					else if (0 == token.compareTo("debug_print")) // optional
						System.out.println("" + subway);
					else
						exit("bad command " + token);

				} else if (STRINGYSTUFF == data_seq) {
					/*
					 * Insert vertex into the graph (or update its satellite data).
					 * (The token contains the satellite data string.)
					 */
					subway.insert(new Point(x1, y1), token);
					cmd_state = RESET;

				} else {
					int coord = Integer.parseInt(token); // could be x or y

					/*
					 * The data_seq variable tells us which coordinate this is.
					 * For cmd_state of ADJACENT or ROUTE, it proceeds like so:
					 * (start) X1IN  -->  Y1IN  -->  X2IN  -->  Y2IN,
					 * followed by action, RESET.  We use the variables
					 * (x1, y1, x2) to store earlier coordinates, until we have
					 * collected all four.
					 *
					 * For cmd_state of INSERT or DELETE, it goes halfway
					 * through that sequence.
					 */
					if (X1IN == data_seq) { // read first coordinate after cmd
						x1 = coord;
						data_seq = Y1IN;

					} else if (Y1IN == data_seq) { // read 2nd coord after cmd
						y1 = coord;
						if (DELETE == cmd_state) {
							/*
							 * That is enough coordinates!
							 * Now delete the vertex from the graph.
							 */
							subway.delete(new Point(x1, y1));
							cmd_state = RESET;
						} else if (INSERT == cmd_state)
							data_seq = STRINGYSTUFF;
						else
							data_seq = X2IN; // for ADJACENT and ROUTE cmds

					} else if (X2IN == data_seq) { // read 3rd coordinate
						x2 = coord;
						data_seq = Y2IN;

					} else { // read 4th coordinate
						assert Y2IN == data_seq : data_seq;
						if (ADJACENT == cmd_state) {
							/*
							 * Insert edge into the graph.
							 */
							subway.adjacent(new Point(x1, y1),
											new Point(x2, coord));
						} else {
							assert ROUTE == cmd_state : cmd_state;
							/*
							 * Request a route from point (x1,y1) to (x2,coord),
							 * in string form.
							 */
							String route = subway.route(new Point(x1, y1),
														new Point(x2, coord));
							System.out.println(route);
						}
						cmd_state = RESET;
					}
				}
			}
	}
}

