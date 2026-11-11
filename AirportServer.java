/**
 * Copyright Airport Management System - étudiants L3 Informatique CY Paris Université
 * @author RATSIMBASON Fanantenana, RABENANDRASANA Hajatiana & ANDRIAMANANTENASOA Aina Mirija
 * @version 1.0
 * @since 07/11/2026
 * Serveur applicatif du protocole AMS-P (TCP, séparateur des trames est ";").
 * Version sans base de donnees : les réponses sont simulées
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

public class AirportServer {
    public static void main(String[] args) {
        int port = 50000;

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Serveur Applicatif (Multi-thread) démarre sur le port " + port + "...");
            // Le serveur écoute a l'infini les nouvelles connexions
            while (true) {
                System.out.println("En attente de connexion d'une nouvelle borne...");
                Socket clientSocket = serverSocket.accept();
                System.out.println("=> Nouvelle connexion entrante ! IP : " + clientSocket.getInetAddress());
                // On délègue la gestion de cette borne à un nouveau Thread
                ClientHandler clientThread = new ClientHandler(clientSocket);
                clientThread.start();
            }
        } catch (IOException e) {
            System.err.println("Erreur fatale sur le serveur : " + e.getMessage());
        }
    }
}

// Gère UNE borne
class ClientHandler extends Thread {
    private Socket socket;

    // état de la session : 0 = rien, 1 = borne identifiee, 2 = reservation ok, 3 = bagage ok
    private int etat = 0;
    private String nom = "";
    private String prenom = "";
    private int nbSoutes = 0;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    public void run() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true)
        ) {
            String message;
            boolean sessionActive = true;

            while (sessionActive && (message = reader.readLine()) != null) {
                System.out.println("[Thread " + Thread.currentThread().getId() + " - Recu] : " + message);

                // on découpe la trame, le -1 garde les champs vides (ex : "SOUTE;")
                String[] parts = message.trim().split(";", -1);
                String commande = parts[0];

                if (commande.equals("ID_BORNE")) {
                    if (parts.length != 2 || parts[1].isEmpty()) {
                        writer.println("Refuse;FORMAT");
                    } else if (etat != 0) {
                        writer.println("Refuse;ORDRE_INVALIDE");
                    } else {
                        etat = 1;
                        writer.println("Acceptee;BORNE_ACCEPTEE");
                    }
                }
                else if (commande.equals("ID_RESA")) {
                    // trame : ID_RESA;reference;nom;prenom
                    if (parts.length != 4 || parts[1].isEmpty() || parts[2].isEmpty() || parts[3].isEmpty()) {
                        writer.println("Refuse;FORMAT");
                    } else if (etat != 1) {
                        writer.println("Refuse;ORDRE_INVALIDE");
                    } else if (parts[1].equals("INCONNU")) {
                        writer.println("Refuse;RESA_INTROUVABLE");
                    } else if (parts[1].equals("ANNULE")) {
                        writer.println("Refuse;VOL_ANNULE");
                    } else {
                        nom = parts[2];
                        prenom = parts[3];
                        etat = 2;
                        writer.println("Acceptee;AF123;A_L_HEURE;14C");
                    }
                }
                else if (commande.equals("SOUTE")) {
                    // trame : SOUTE;21.5
                    if (parts.length != 2) {
                        writer.println("Refuse;FORMAT");
                    } else if (etat != 2 && etat != 3) {
                        writer.println("Refuse;ORDRE_INVALIDE");
                    } else {
                        try {
                            double poids = Double.parseDouble(parts[1]);
                            // parseDouble accepte aussi "NaN"="Not-a-Number" et "Infinity", on les refuse
                            if (Double.isNaN(poids) || Double.isInfinite(poids) || poids <= 0) {
                                writer.println("Refuse;POIDS_INVALIDE");
                            } else if (nbSoutes >= 2) {
                                writer.println("Refuse;TROP_DE_BAGAGES");
                            } else if (poids > 32.0) {
                                writer.println("Refuse;SURPOIDS");
                            } else {
                                nbSoutes++;
                                etat = 3;
                                writer.println("Acceptee;ETIQ00" + nbSoutes);
                            }
                        } catch (NumberFormatException e) {
                            writer.println("Refuse;POIDS_INVALIDE");
                        }
                    }
                }
                else if (commande.equals("CABINE")) {
                    // trame : CABINE;longueur;hauteur;profondeur
                    if (parts.length != 4) {
                        writer.println("Refuse;FORMAT");
                    } else if (etat != 2 && etat != 3) {
                        writer.println("Refuse;ORDRE_INVALIDE");
                    } else {
                        try {
                            int l = Integer.parseInt(parts[1]);
                            int h = Integer.parseInt(parts[2]);
                            int p = Integer.parseInt(parts[3]);
                            if (l <= 0 || h <= 0 || p <= 0) {
                                writer.println("Refuse;DIMENSIONS_INVALIDES");
                            } else if (l > 55 || h > 40 || p > 25) {
                                writer.println("Refuse;CABINE_TROP_GRAND");
                            } else {
                                etat = 3;
                                writer.println("Acceptee;CABINE_ACCEPTEE");
                            }
                        } catch (NumberFormatException e) {
                            writer.println("Refuse;DIMENSIONS_INVALIDES");
                        }
                    }
                }
                else if (commande.equals("CARTE_EMBARQUEMENT")) {
                    if (etat != 3) {
                        writer.println("Refuse;ORDRE_INVALIDE");
                    } else {
                        // le client Python attend 6 infos separees par des ";"
                        writer.println("Acceptee;AF123;" + nom + ";" + prenom + ";14C;E13");
                    }
                }
                else if (commande.equals("FERMETURE")) {
                    writer.println("Fait !;FERMETURE");
                    sessionActive = false;
                }
                else {
                    writer.println("Refuse;COMMANDE_INCONNUE");
                }
            }
            System.out.println("=> Fin de session pour le Thread " + Thread.currentThread().getId());
            socket.close();
        } catch (IOException e) {
            System.err.println("Erreur de communication avec la borne : " + e.getMessage());
        }
    }
}