package controller.server;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Vector;

import model.server.ConnectedClients;
import model.server.ConnectedServers;
import view.ServerFrame;
import controller.client.ClientInterface;

public class Server extends UnicastRemoteObject implements ServerInterface {

	private static final long serialVersionUID = 8405014344648483674L;
	private static final String HOST = "localhost";
	public static final String URL_STRING = "rmi://" + HOST + "/Server/";
	private final String serverNameString;
	private final ServerFrame gui; // VIEW

	/**** RISORSE CONDIVISE DA SINCRONIZZARE *****/
	private final ConnectedClients connectedClients = new ConnectedClients(); // MODEL
	private final ConnectedServers connectedServers = new ConnectedServers(); // MODEL
	/*********************************************/
	
	private final Object clientsMonitor = new Object();
	private final Object serversMonitor = new Object();
	private final ClientChecker clientsChecker;
	private final ServerChecker serversChecker;

	public Server(final String paramServerName) throws RemoteException {
		super();
		serverNameString = paramServerName;
		// creo gui
		gui = new ServerFrame(paramServerName, connectedClients, connectedServers);
		// dico al MODEL chi e' il suo Observer
		this.connectedClients.addObserver(gui);
		this.connectedServers.addObserver(gui);
		// set the java.rmi.server.hostname property to tell the RMI Registry which hostname or IP Adress to return in its RMI URLs: http://stackoverflow.com/questions/11343132/rmi-responding-very-slow
		String ipAddress = "127.0.0.1"; //Local IP address 
		System.setProperty("java.rmi.server.hostname",ipAddress);
		
		// quando si chiude il server si deve disconnettere dall rmi registry
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				try {
					disconnect();
				} catch (MalformedURLException | RemoteException | NotBoundException e) {
					e.printStackTrace();
				}
			}
		});
		
		// faccio partire i controllori dei client e server connessi
		clientsChecker = new ClientChecker(clientsMonitor, connectedClients);
		serversChecker = new ServerChecker(serversMonitor, connectedServers);
		clientsChecker.start();
		serversChecker.start();
	}

	@Override
	public Integer clientConnect(ClientInterface clientToConnect) throws RemoteException {
		System.out.println(clientToConnect.getClientName() + " try to connect");
		Integer functionResultInteger = -1;
		synchronized (clientsMonitor) {
			// check if client is already connected
			if (!connectedClients.getConnectedClients().contains(clientToConnect)) {
				connectedClients.addClient(clientToConnect);
				functionResultInteger = 1;
			}
			// risveglia il thread clientChecker
			clientsMonitor.notifyAll();
		}
		System.out.println(functionResultInteger);
		return functionResultInteger;
	}

	@Override
	public Integer clientDisconnect(ClientInterface clientToDisconnect) throws RemoteException {
		System.out.println(clientToDisconnect.getClientName() + " try to disconnect");
		Integer functionResultInteger = -1;
		synchronized (clientsMonitor) {
			// check if client is connected
			if (connectedClients.getConnectedClients().contains(clientToDisconnect)) {
				connectedClients.removeClient(clientToDisconnect);
				functionResultInteger = 0;
			}
			// risveglia il thread clientChecker
			clientsMonitor.notifyAll();
		}
		System.out.println(functionResultInteger);
		return functionResultInteger;
	}

	@Override
	public void disconnect() throws NotBoundException, MalformedURLException, RemoteException {
		Naming.unbind(Server.URL_STRING + serverNameString);
	}

	@Override
	public Vector<ClientInterface> getClients() throws RemoteException {
		return connectedClients.getConnectedClients();			
	}

	@Override
	public String getServerNameString() throws RemoteException {
		return serverNameString;
	}

	@Override
	public String getServerUrl() throws RemoteException {
		return Server.URL_STRING + serverNameString;
	}

	@Override
	public Vector<ClientInterface> getResourceOwners(final String paramResourceName, final String clientCaller) throws RemoteException {
		final Vector<ClientInterface> searchedResourceOwners = new Vector<ClientInterface>();
		synchronized (serversMonitor) {	
			for (final ServerInterface serverInterface : connectedServers.getConnectedServers()) {
				for (final ClientInterface cli : serverInterface.getClients()) {
					gui.appendLogEntry("Looking for " + paramResourceName + " in " + cli.getClientName() + "@" + serverInterface.getServerNameString());
					if (cli.checkResourcePossession(paramResourceName, clientCaller)) {
						gui.appendLogEntry(cli.getClientName() + "@" + serverInterface.getServerNameString() + " has " + paramResourceName);
						searchedResourceOwners.add(cli);
					}		
				}
			}
		}
		return searchedResourceOwners;
	}
}