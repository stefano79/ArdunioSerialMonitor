package com.wordpress.blogste.serialmonitor;

import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;

import java.awt.EventQueue;


import java.io.*;
import java.util.Enumeration;
import java.util.Properties;


import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;


import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import java.awt.Component;
import javax.swing.JMenu;
import javax.swing.UIManager;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JPanel;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.event.MenuEvent;
import javax.swing.JCheckBox;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JCheckBoxMenuItem;
import java.awt.Toolkit;
import javax.swing.event.MenuListener;


public class ArduinoSerialMonitor implements SerialPortEventListener {

	private JFrame frmSerialMonitor;

	SerialPort serialPort;
	private InputStream input;						//Buffered input stream from the port
	private OutputStream output;					//The output stream to the port
	private static final int TIME_OUT = 2000;		//Milliseconds to block while waiting for port open
	private int dataRate;							//Default bits per second for serial port.
	
	private JTextField textSend;
	private JTextArea textArea;
	private boolean serialIsOpen = false;
	private JScrollPane scrollPane;
	private JPanel panel_1;
	private JButton btnConnetti;
	private JCheckBox chckbxAutoscroll;
	private String[] ARRAY_STRING_DATARATE = {
		      "300","1200","2400","4800","9600","14400",
		      "19200","28800","38400","57600","115200"
		    };
	private JMenuBar menuBar;
	private JMenu mnSerialPort;
	private JMenu mnBaudrate;
	private SelectPortListner selectPortListner;
	private SelectBaudrate selectBaudrate;
	static final String CONGIG_FILE = "config.txt";
	private static String pathConfigFile;
	static Properties fileConfig = new Properties();
	private JPopupMenu popupMenu;
	private JPopupMenu popupMenu_1;
	private JMenuItem mntmCopia;
	private JMenuItem mntmIncolla;
	private JMenuItem mntmCopia_1;


	/**
	 * Launch the application.
	 * @throws IOException 
	 * @throws FileNotFoundException 
	 */
	public static void main(String[] args) throws FileNotFoundException, IOException {
		try {
			System.setProperty("apple.laf.useScreenMenuBar", "true");					//Abilitazione il menu sulla barra di OSX
			System.setProperty("com.apple.mrj.application.apple.menu.about.name",
					"Arduino Serial Monitor");													//Settaggio nome applicazione sella barra di OSX
		} catch (Throwable e){
			e.printStackTrace();
		}
		
		try {			
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());		//Abilitazione look oggetti come il sistema
			} catch (Throwable e) {
			e.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ArduinoSerialMonitor window = new ArduinoSerialMonitor();
					window.frmSerialMonitor.setVisible(true);

				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		pathConfigFile = System.getProperty("user.home") + "/.ArduinoSerialMonitor/" + CONGIG_FILE;
		try {
	    fileConfig.load(new FileInputStream(pathConfigFile));							//Caricamento file sull' oggetto propietes
		} catch (FileNotFoundException e) {
			new File(System.getProperty("user.home") + "/.ArduinoSerialMonitor").mkdir();
			fileConfig.setProperty("name", "");											//Se il caricamento dell' oggeto propietes non va a buon fine viene creato un file di deafault
			fileConfig.setProperty("baudrate", "9600");									
		    fileConfig.store(new FileOutputStream(pathConfigFile), null);				
		}

	}
	
	

	/**
	 * Create the application.
	 */
	public ArduinoSerialMonitor() {
		initialize();
		
	}

	
	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
	
		
		frmSerialMonitor = new JFrame();
		frmSerialMonitor.setIconImage(Toolkit.getDefaultToolkit().getImage(ArduinoSerialMonitor.class.getResource("/com/sun/java/swing/plaf/windows/icons/JavaCup32.png")));
		frmSerialMonitor.setTitle("Arduino Serial Monitor");
		frmSerialMonitor.setBounds(100, 100, 533, 392);
		frmSerialMonitor.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		
		panel_1 = new JPanel();
		
		textSend = new JTextField();
		textSend.setColumns(10);
		
		JButton btnScrivi = new JButton("Invia");
		btnScrivi.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				writeSerial();
			}
		});
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addComponent(textSend, GroupLayout.DEFAULT_SIZE, 389, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(btnScrivi, GroupLayout.PREFERRED_SIZE, 121, GroupLayout.PREFERRED_SIZE)
					.addGap(0))
		);
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
					.addComponent(textSend, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
					.addComponent(btnScrivi))
		);
		
		popupMenu = new JPopupMenu();
		addPopup(textSend, popupMenu);
		
		mntmCopia = new JMenuItem("Copia");
		mntmCopia.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textSend.copy();
			}
		});
		popupMenu.add(mntmCopia);
		
		mntmIncolla = new JMenuItem("Incolla");
		mntmIncolla.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textSend.paste();
			}
		});
		popupMenu.add(mntmIncolla);
		panel_1.setLayout(gl_panel_1);
		
		JPanel panel_2 = new JPanel();
		
		scrollPane = new JScrollPane();
		scrollPane.setAutoscrolls(true);
		
		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		
		popupMenu_1 = new JPopupMenu();
		addPopup(textArea, popupMenu_1);
		
		mntmCopia_1 = new JMenuItem("Copia");
		mntmCopia_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.copy();
			}
		});
		popupMenu_1.add(mntmCopia_1);
		
		chckbxAutoscroll = new JCheckBox("Scorrimento automatico");
		chckbxAutoscroll.setSelected(true);
		
		btnConnetti = new JButton("Connetti");
		btnConnetti.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (serialIsOpen == false) {
					openSerialPort(fileConfig.getProperty("name"));
					if (serialIsOpen)btnConnetti.setText("Disconetti");
				} else {
					closeSerialPort();
					btnConnetti.setText("Conetti");
				}
			}
		});
		
		JButton btnPulisci = new JButton("Pulisci");
		btnPulisci.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				textArea.setText("");
			}
		});
		GroupLayout gl_panel_2 = new GroupLayout(panel_2);
		gl_panel_2.setHorizontalGroup(
			gl_panel_2.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_2.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 521, Short.MAX_VALUE)
						.addGroup(gl_panel_2.createSequentialGroup()
							.addComponent(chckbxAutoscroll)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(btnPulisci)
							.addPreferredGap(ComponentPlacement.RELATED, 113, Short.MAX_VALUE)
							.addComponent(btnConnetti, GroupLayout.PREFERRED_SIZE, 110, GroupLayout.PREFERRED_SIZE)))
					.addContainerGap())
		);
		gl_panel_2.setVerticalGroup(
			gl_panel_2.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_panel_2.createSequentialGroup()
					.addContainerGap()
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 258, Short.MAX_VALUE)
					.addGap(7)
					.addGroup(gl_panel_2.createParallelGroup(Alignment.BASELINE)
						.addComponent(chckbxAutoscroll)
						.addComponent(btnConnetti)
						.addComponent(btnPulisci))
					.addContainerGap())
		);
		panel_2.setLayout(gl_panel_2);
		GroupLayout groupLayout = new GroupLayout(frmSerialMonitor.getContentPane());
		groupLayout.setHorizontalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 533, Short.MAX_VALUE)
				.addComponent(panel_1, Alignment.TRAILING, GroupLayout.DEFAULT_SIZE, 533, Short.MAX_VALUE)
		);
		groupLayout.setVerticalGroup(
			groupLayout.createParallelGroup(Alignment.LEADING)
				.addGroup(groupLayout.createSequentialGroup()
					.addContainerGap()
					.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 334, Short.MAX_VALUE)
					.addGap(0))
		);
		frmSerialMonitor.getContentPane().setLayout(groupLayout);
		
		menuBar = new JMenuBar();
		frmSerialMonitor.setJMenuBar(menuBar);
		
		JMenu mnOpzioni = new JMenu();
		mnOpzioni.addMenuListener(new MenuListener() {
			public void menuCanceled(MenuEvent arg0) {
			}
			public void menuDeselected(MenuEvent arg0) {
			}
			public void menuSelected(MenuEvent arg0) {
				populateSerialMenu();
			}
		});
		mnOpzioni.setText("Opzioni");
		menuBar.add(mnOpzioni);
		
		mnSerialPort = new JMenu();
		mnSerialPort.setText ("Porta Seriale");
		mnOpzioni.add(mnSerialPort);
		selectPortListner  = new SelectPortListner();
		populateSerialMenu();
		
		mnBaudrate = new JMenu();
		mnBaudrate.setText ("Baudrate");
		mnOpzioni.add(mnBaudrate);
		selectBaudrate = new SelectBaudrate();
		populateBaudrateMenu();
	}
	
	
	 /**
	   * Questo metodo popola il menu con la lista delle porte di comunicazione
	   * disponibili.
	   */
	protected void populateSerialMenu() {
	  Enumeration portEnum = CommPortIdentifier.getPortIdentifiers(); 														 //creo la lista di oggetti CommPortIdentifier per ogni porta presente
	  
		mnSerialPort.removeAll();
		JCheckBoxMenuItem chekBoxPortaSeriale;
	  	
	    // Scandisce tutte le porte presenti per caricare il menu
	    while (portEnum.hasMoreElements())																					//ciclo while fino a quando ci sono elemnti in portEnum
	    {
	      CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();										//creo l' oggetto currPortId assegnadogli un oggetto della lista portEnum
	      chekBoxPortaSeriale = new JCheckBoxMenuItem(currPortId.getName());												//creazione JCheckBoxMenuItem da aggiungere al menu
	      chekBoxPortaSeriale.addActionListener(selectPortListner);															//aggiunzione ActionListener al JCheckBoxMenuItem da aggiungere al menu
	      if (chekBoxPortaSeriale.getText().equals(fileConfig.getProperty("name"))) chekBoxPortaSeriale.setState(true);		//settaggio porta seriale di default
	      mnSerialPort.add(chekBoxPortaSeriale);																			//aggiunzione JCheckBoxMenuItem al menu
	    }
	}
	/**
	   * Questo metodo popola il menu con la lista delle velocitˆ di comunicazione
	   * impostate nell' array ARRAY_STRING_DATARATE
	   */
	protected void populateBaudrateMenu() {
		
		JCheckBoxMenuItem chekBoxBaudrate;
		
		for (int i = 0; i < ARRAY_STRING_DATARATE.length; i++){
			chekBoxBaudrate = new JCheckBoxMenuItem(ARRAY_STRING_DATARATE[i] + " " + "baud");
			chekBoxBaudrate.addActionListener(selectBaudrate);																				//aggiunzione ActionListener al JCheckBoxMenuItem da aggiungere al menu
		    if (chekBoxBaudrate.getText().equals(fileConfig.getProperty("baudrate") + " " + "baud")) chekBoxBaudrate.setState(true);		//settaggio baudrate porta seriale di default
		    mnBaudrate.add(chekBoxBaudrate);																								//aggiunzione JCheckBoxMenuItem al menu
		}
	}
	/**
	   * Questo metodo apre una connessione con la porta seriale che gli viene
	   * passata come argomento.
	   */
	public void openSerialPort(String nameSerialPort) {
		
		serialIsOpen = false;
		dataRate = Integer.parseInt(fileConfig.getProperty("baudrate"));
		CommPortIdentifier portId = null;														//creo l' oggetto portId
		Enumeration portEnum = CommPortIdentifier.getPortIdentifiers();							//creo la lista di oggetti CommPortIdentifier per ogni porta presente
		// iterate through, looking for the port
		while (portEnum.hasMoreElements()) {													//ciclo while fino a quando ci sono elemnti in portEnum
			CommPortIdentifier currPortId = (CommPortIdentifier) portEnum.nextElement();		//creo l' oggetto currPortId assegnadogli un oggetto della lista portEnum
				if (currPortId.getName().equals(nameSerialPort)) {								//Se il nome della porta dell' oggetto currPortId  uguale alla porta passata al metodo
					portId = currPortId;														//copio l'oggetto  currPortId in portId
					
				}
			}

		if (portId == null) {																	//Se la porta passata al metodo non  in nessuno oggetto di portEnum si esce dal metodo
			JOptionPane.showMessageDialog(null, "Porta: "+nameSerialPort + " non trovata.");
			serialIsOpen = false;
			return;
		}

		try {
			// open serial port, and use class name for the appName.
			serialPort = (SerialPort) portId.open(this.getClass().getName(),
					TIME_OUT);
			serialPort.setDTR(false);

			// set port parameters
			serialPort.setSerialPortParams(dataRate,
					SerialPort.DATABITS_8,
					SerialPort.STOPBITS_1,
					SerialPort.PARITY_NONE);

			// open the streams
			input = serialPort.getInputStream();
			output = serialPort.getOutputStream();

			// add event listeners
			serialPort.addEventListener(this);
			serialPort.notifyOnDataAvailable(true);
			serialIsOpen = true;
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null,"La porta selezionata  in errore: " + e);
			serialIsOpen = false;
		}
	}	
	/**
	   *Questo metodo chiude la comunicazioe con la porta seriale
	   */
	public synchronized void closeSerialPort() {
		if (serialPort != null) {
			serialPort.removeEventListener();
			serialPort.close();
			serialIsOpen = false;
		}
	}
	/**
	   *Questo metodo  un thread che viene lanciato ogni volta che c' un evento
	   *sulla seriale
	   */
	public synchronized void serialEvent(SerialPortEvent oEvent) {
		if (oEvent.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
			try {
				int available = input.available();										//Creazione variabile che indica il numero di byte nel buffer di input
				byte chunk[] = new byte[available];										//Creazione array di byte con indice uguale al numero di byte nel buffer di input
				input.read(chunk, 0, available);										//Popolamento dell' array chunk con i byte presenti nel buffer di input
				textArea.append(new String(chunk));										//Scrittura codice ascii dell' array di byte chunk nel monitor seriale
				if (chckbxAutoscroll.isSelected()) {
					textArea.setCaretPosition(textArea.getDocument().getLength());		//Per abilitare l' autoscrool viene settato il cursore all' ultima riga presente
				}
				} catch (Exception e) {
				System.err.println(e.toString());
		
			}
		}
	}
	/**
	   *Questo metodo scrive nel buffer della seriale
	   */
	public void writeSerial() {
		
		try {
			String stringa = textSend.getText();		//creazione la stringa da inviare tramite seriale
			byte chunk[] = stringa.getBytes();			//creazione l' array di byte che compongono la stringa da inviare
			int lenght = chunk.length;					//calcolo la lunghezza dell' array
			output.write(chunk, 0, lenght);				//scrittura array di byte da inviare nel buffer di uscita della seriale
		} catch (Exception e) {
		}
	}
	/**
	   *Questa classe gestisce la selezione della porta seriale.
	   *Ogni singola voce del menu deve essere implementata con questa classe.
	   */
	class SelectPortListner implements ActionListener {

	    public void actionPerformed(ActionEvent e) {
	
		String nomePorta = ((JCheckBoxMenuItem)e.getSource()).getText();
		for (int i = 0; i < mnSerialPort.getItemCount(); i++) {
			JCheckBoxMenuItem item = ((JCheckBoxMenuItem)mnSerialPort.getItem(i));
			if (item.getText().equals(nomePorta)) {
				item.setState(true);
				fileConfig.setProperty("name", nomePorta);
			    try {
					fileConfig.store(new FileOutputStream(pathConfigFile), null);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else {
				item.setState(false);
			}
		}
	  }
	}
	/**
	   *Questa classe gestisce la selezione della velocitˆ della porta seriale.
	   *Ogni singola voce del menu deve essere implementata con questa classe.
	   */	
	class SelectBaudrate implements ActionListener {

	    public void actionPerformed(ActionEvent e) {
	
		String baudrate = ((JCheckBoxMenuItem)e.getSource()).getText();
		for (int i = 0; i < mnBaudrate.getItemCount(); i++) {
			JCheckBoxMenuItem item = ((JCheckBoxMenuItem)mnBaudrate.getItem(i));
			if (item.getText().equals(baudrate)) {
				item.setState(true);
				fileConfig.setProperty("baudrate", ARRAY_STRING_DATARATE[i]);
			    try {
					fileConfig.store(new FileOutputStream(pathConfigFile), null);
				} catch (FileNotFoundException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (IOException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			} else {
				item.setState(false);
			}
		}
	  }
	}
	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}
			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}
}