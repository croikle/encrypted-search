import java.io.*;
import java.net.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.*;

/** 
    Client for CSE 667 Project 2
    Team 4:
    Peter McLarhan
    Benjamin Rogers
    Jun Wang
 **/
class ClientGUI extends JFrame implements ActionListener{

    static int SEARCH_FLAG = 0;
    static int FILE_FLAG = 1;
    static int ERROR_FLAG = -1;
    static String CONFIG_FILE = "client.config";
 
    String host;
    int sock;
    String directory;

    JFrame frame = new JFrame();
    JPanel pane = new JPanel();
    JLabel hostLabel = new JLabel("Hostname:");
    JLabel sockLabel = new JLabel("Port Number:");
    JTextField hostText = new JTextField(20);
    JTextField sockText = new JTextField(20);
    JTextField searchText = new JTextField(20);
    JButton searchButton = new JButton("Search");
    JTextField fileText = new JTextField(20);
    JButton fileButton = new JButton("Upload File");
    JTextField directoryText = new JTextField(20);
    JButton directoryButton = new JButton("Set Directory");

    // sets up a very basic GUI
    ClientGUI() {
	super("Remote Storage Client");
	setBounds(100,100,400,200);
	setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	pane.setLayout(new GridLayout(5, 2, 5, 10));
	Container con = this.getContentPane();
	con.add(pane);

	// Read in the config file
	try {
	    FileInputStream fstream = new FileInputStream(CONFIG_FILE);
	    DataInputStream in = new DataInputStream(fstream);
	    BufferedReader br = new BufferedReader(new InputStreamReader(in));
	    String strLine;
	    host = br.readLine().substring(7);
	    sock = Integer.parseInt(br.readLine().substring(9));
	    directory = br.readLine().substring(12);
	    
	} catch (Exception e) {
	    System.err.println("Execption: " + e.getMessage());
	    JOptionPane.showMessageDialog(null, e.getMessage(), "IO Error", JOptionPane.ERROR_MESSAGE);
	    host = "localhost";
	    sock = 6789;
	    directory = System.getProperty("user.dir");
	}

	hostText.setText(host);
	sockText.setText(sock + "");
	directoryText.setText(directory);

	hostText.addActionListener(this);
	sockText.addActionListener(this);
	searchText.addActionListener(this);
	searchButton.addActionListener(this);
	fileText.addActionListener(this);
	fileButton.addActionListener(this);
	directoryText.addActionListener(this);
	directoryButton.addActionListener(this);
	pane.add(hostLabel);
	pane.add(sockLabel);
	pane.add(hostText);
	pane.add(sockText);
	pane.add(searchText);
	pane.add(searchButton);
	pane.add(fileText);
	pane.add(fileButton);
	pane.add(directoryText);
	pane.add(directoryButton);
	setVisible(true);
    }

    // When buttons are pressed
    public void actionPerformed(ActionEvent event) {

	ArrayList<String> message = new ArrayList<String>();
	message.clear();
	
	Object source = event.getSource();

	// if a search is requested
	// TODO
	if (source == searchButton) {

	    message.add(searchText.getText());
	    
	    // Send the search text
	    try {
		connect(host, sock, message, SEARCH_FLAG);
	    } catch (Exception e) {
		System.err.println("Execption: " + e.getMessage());
		JOptionPane.showMessageDialog(null, e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
	    }
	     setVisible(true);
	}

	// if a file upload is requested
	// TODO
	if (source == fileButton) {

	    // Read in the file
	    try {
		ReadFile(fileText.getText(), message);
	    } catch (Exception e) {
		System.err.println("Execption: " + e.getMessage());
		JOptionPane.showMessageDialog(null, e.getMessage(), "IO Error", JOptionPane.ERROR_MESSAGE);
		message.add("ERROR");
	    }
	    
	    // Send the file text
	    try {
		connect(host, sock, message, FILE_FLAG);
	    } catch (Exception e) {
		System.err.println("Execption: " + e.getMessage());
		JOptionPane.showMessageDialog(null, e.getMessage(), "Connection Error", JOptionPane.ERROR_MESSAGE);
	    }
	     setVisible(true);
	}
    }

    // reads in a file, the lines are added the the message arraylist
    public void ReadFile(String filename, ArrayList<String> message) throws Exception{

	message.add(filename);

	FileInputStream fstream = new FileInputStream(directory + filename);
	DataInputStream in = new DataInputStream(fstream);
	BufferedReader br = new BufferedReader(new InputStreamReader(in));
	String strLine;
	while ((strLine = br.readLine()) != null) {
	    message.add(strLine);
	}
    }

    // connects to the server and sends the message
    public void connect(String host, int sock, ArrayList<String> message, int flag) throws Exception {

	String sentence;
	String modifiedSentence;
	int size;
	BufferedReader inFromUser = new BufferedReader( new InputStreamReader(System.in));
	Socket clientSocket = new Socket(host, sock);
	DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
	BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

	sentence = flag + "\n" + message.size() + "\n";
	for (int i = 0; i < message.size(); i++) {
	    sentence += message.get(i) + "\n";
	}

	outToServer.writeBytes(sentence);
	modifiedSentence = inFromServer.readLine();

	// if File message
	if (FILE_FLAG == Integer.parseInt(modifiedSentence)) {
	    modifiedSentence = inFromServer.readLine();
	    // if Error message
	    if (ERROR_FLAG == Integer.parseInt(modifiedSentence)) {
		JOptionPane.showMessageDialog(null, "Error writing file", "Server Error", JOptionPane.ERROR_MESSAGE);
	    } else {
		JOptionPane.showMessageDialog(null, "Successful file write", "Server Message", JOptionPane.PLAIN_MESSAGE);
	    }
	}
	// if Search message
	if (SEARCH_FLAG == Integer.parseInt(modifiedSentence)) {
	    modifiedSentence = inFromServer.readLine();
	    // if Error message
	    if (ERROR_FLAG == Integer.parseInt(modifiedSentence)) {
		JOptionPane.showMessageDialog(null, "Error searching for word", "Server Error", JOptionPane.ERROR_MESSAGE);
	    } else {
		size = Integer.parseInt(inFromServer.readLine());
		message.clear();
		sentence = "";

		for (int i = 0; i < size; i++) {
		    message.add(inFromServer.readLine());
		    sentence += "\n" + message.get(i);
		}

		// Choose to weather or not to download found files
		Object[] options = {"Download", "Cancel"};
		int n = JOptionPane.showOptionDialog(frame, size + " files with matches:" + sentence + "\nDownload files?", "Server Message", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
		if (n == JOptionPane.YES_OPTION) {
		    outToServer.writeBytes(FILE_FLAG + "\n" + size + sentence + "\n");
		    try {
			int fSize;
			// download files
			for (int i = 0; i < message.size(); i++) {
			    fSize = Integer.parseInt(inFromServer.readLine());
			    if (fSize == ERROR_FLAG) {
				throw new Exception("Server Error");
			    } else {
				FileWriter fstream = new FileWriter(directory + message.get(i));
				BufferedWriter out = new BufferedWriter(fstream);
				for (int j = 0; j < fSize; j++) {
				    out.write(inFromServer.readLine() + "\n");			
				}
				out.close();
			    }
			}
		    } catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Error download files", "Server Error", JOptionPane.ERROR_MESSAGE);
		    }
		} else {
		    outToServer.writeBytes(ERROR_FLAG + "\n");
		}
	    }
	}

	clientSocket.close();

    }

    // initializes the GUI
    public static void main(String argv[]){

	new ClientGUI();

    }
}