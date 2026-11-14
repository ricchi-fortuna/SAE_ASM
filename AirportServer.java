/**
 * Copyright Airport Management System - étudiants L3 Informatique CY Paris Université
 * @author RATSIMBASON Fanantenana, RABENANDRASANA Hajatiana & ANDRIAMANANTENASOA Aina Mirija
 * @version 2.0
 * @since 14/11/2026
 * Serveur applicatif séquentiel du protocole AMS-P sur TCP.
 * Version sans base de donnees : les réponses sont simulées.
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
            System.out.println("Serveur Applicatif AMS-P démarre sur le port " + port + "...");
            // Boucle principale : le serveur traite une borne
            while (true) {
                System.out.println("En attente de connexion d'une borne...");
                try (Socket clientSocket = serverSocket.accept();
                     BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                     PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true)){
                    System.out.println("Connexion de la borne : "+ clientSocket.getInetAddress());

                    // Variable de la communication réinitialisée pour la connexion d'une borne
                    // état de la session : 0 = rien, 1 = borne identifiee, 2 = reservation ok, 3 = bagage ok
                    int etat = 0;
                    String nom = "";
                    String prenom = "";
                    int nbSoutes = 0;
                    boolean sessionActive = true;
                    String message;

                    while(sessionActive && (message = reader.readLine()) != null) {
                        System.out.println("Recu : " + message);

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
                        } else if (commande.equals("ID_RESA")) {
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
                        } else if (commande.equals("SOUTE")) {
                            // trame : SOUTE;21.5
                            if (parts.length != 2) {
                                writer.println("Refuse;FORMAT");
                            } else if (etat != 2 && etat != 3) {
                                writer.println("Refuse;ORDRE_INVALIDE");
                            } else {
                                try {
                                    double poids = Double.parseDouble(parts[1]);
                                    // parseDouble accepte si "NaN"="Not-a-Number" et "Infinity", on les refuse
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
                        } else if (commande.equals("CABINE")) {
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
                        } else if (commande.equals("CARTE_EMBARQUEMENT")) {
                            // Autorisé dès que la réservation est validée
                            if (etat !=2 && etat != 3) {
                                writer.println("Refuse;ORDRE_INVALIDE");
                            } else {
                                // le client Python attend 6 infos separees par des ";"
                                writer.println("Acceptee;AF123;" + nom + ";" + prenom + ";14C;E13");
                            }
                        } else if (commande.equals("FERMETURE")){
                            writer.println("Fait !;FERMETURE");
                            sessionActive = false;
                        } else {
                            writer.println("Refuse;COMMANDE_INCONNUE");
                        }
                    }
                    System.out.println("Fin de la connexion avec la borne.");
                } catch (IOException e) {
                    System.err.println("Erreur de communication avec la borne : " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur au lancement du serveur : " + e.getMessage());
        }
    }
}