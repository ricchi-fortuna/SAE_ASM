"""
Airport Management System - Client TCP
Projet SAE L3 Informatique - CY Cergy Paris Université

Client de communication avec le serveur applicatif Java.
Protocole AMS-P : TCP avec séparateur ";"
"""

import socket

adresseIP = "127.0.0.1"
port = 50000


# Fonction qui ferme la connexion entre le client et le serveur
def fermer_connexion():
    socket_client.sendall("FERMETURE\n".encode("utf-8"))
    reponse = recevoir_message()
    print(reponse)
    socket_client.close()
    exit()


# Réception d'un message complet terminé par "\n"
# Un recv(1024) ne garantit pas la réception du message entier
def recevoir_message():
    bmessage = b""

    while not bmessage.endswith(b"\n"):
        morceau = socket_client.recv(1)

        if not morceau:
            return ""

        bmessage += morceau

    return bmessage.decode("utf-8").strip()


# Création de la socket TCP en IPv4
socket_client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
socket_client.connect((adresseIP, port))


# Identification de la borne auprès du serveur
# Trame envoyée : ID_BORNE;identifiant
id_borne = "BORNE01"
message = "ID_BORNE;" + id_borne + "\n"
socket_client.sendall(message.encode("utf-8"))

reponse = recevoir_message()

# Vérification de la réponse d'identification
if reponse == "Acceptee;BORNE_ACCEPTEE":
    print("Borne reconnue")
else:
    print("Borne non reconnue")
    fermer_connexion()


# Identification du voyageur
# Trame envoyée : ID_RESA;reference;nom;prenom
enregistrement_valide = False

id_resa = input("Entrez votre code de réservation : ")
nom_voy = input("Entrez votre nom : ")
prenom_voy = input("Entrez votre prenom : ")

message = "ID_RESA;" + id_resa + ";" + nom_voy + ";" + prenom_voy + "\n"
socket_client.sendall(message.encode("utf-8"))

reponse = recevoir_message()

# Vérification de la réservation
# Le serveur peut accepter ou refuser selon le billet et le vol
if reponse.startswith("Acceptee;"):
    enregistrement_valide = True
    print("Réservation acceptée :", reponse[len("Acceptee;"):])
else:
    if reponse == "Refuse;RESA_INTROUVABLE":
        print("Billet introuvable")
    elif reponse == "Refuse;VOL_ANNULE":
        print("Votre vol est annulé")
    else:
        print("Réponse inattendue :", reponse)

    fermer_connexion()


# Vérification des bagages en soute
# Trame envoyée : SOUTE;poids
choix_bagage = input("Avez-vous un bagage en soute ? (oui/non) : ")

if choix_bagage == "oui":
    poids_Soute = input("Veuillez renseigner le poids du bagage : ")

    message = "SOUTE;" + poids_Soute + "\n"
    socket_client.sendall(message.encode("utf-8"))

    reponse = recevoir_message()

    # Si le bagage est accepté, le serveur renvoie une étiquette
    if reponse.startswith("Acceptee;"):
        print("Voici votre numéro d'étiquette :", reponse[len("Acceptee;"):])
    else:
        print("Bagage en soute refusé :", reponse)
        fermer_connexion()


# Vérification du bagage en cabine
# Trame envoyée : CABINE;longueur;hauteur;profondeur
choix_bagage = input("Avez-vous un bagage en cabine ? (oui/non) : ")

if choix_bagage == "oui":
    long = input("Entrer la longueur : ")
    larg = input("Entrer la hauteur : ")
    haut = input("Entrer la profondeur : ")

    message = "CABINE;" + long + ";" + larg + ";" + haut + "\n"
    socket_client.sendall(message.encode("utf-8"))

    reponse = recevoir_message()

    if reponse == "Acceptee;CABINE_ACCEPTEE":
        print("La valise respecte le gabarit")

    elif reponse == "Refuse;CABINE_TROP_GRAND":
        print("Le bagage dépasse le gabarit autorisé")
        fermer_connexion()
    else:
        print("Réponse inattendue :", reponse)
        fermer_connexion()


# Demande de la carte d'embarquement
# Le serveur renvoie les informations du vol et du voyageur
if enregistrement_valide:
    message = "CARTE_EMBARQUEMENT\n"
    socket_client.sendall(message.encode("utf-8"))

    reponse = recevoir_message()
    infos = reponse.split(";")

    # Trame reçue : Acceptee;vol;nom;prenom;siege;porte
    if len(infos) == 6 and infos[0] == "Acceptee":
        print("===== CARTE D'EMBARQUEMENT =====")
        print("Vol :", infos[1])
        print("Nom :", infos[2])
        print("Prénom :", infos[3])
        print("Siège :", infos[4])
        print("Porte :", infos[5])

    else:
        print("Impossible de récupérer la carte d'embarquement")

    # Fermeture de la connexion après l'enregistrement
    fermer_connexion()