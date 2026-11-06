import socket
from symtable import Function

adresseIP = "127.0.0.1"
port = 50000
def fermer_connexion():
    socket_client.close()
    exit()

# utiliser une adresseIpV4 et le tcp
socket_client = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
socket_client.connect((adresseIP, port))

#identification de la borne
id_borne = "BORNE01"
message = id_borne + "\n"
socket_client.sendall(message.encode("utf-8"))

#gestion de la réponse d'identification :
reponse = socket_client.recv(1024)
reponse = reponse.decode("utf-8").strip()

if reponse == "Acceptée":
    print("Borne reconnue")
else:
    print("Borne non reconnue")
    fermer_connexion()

#Identification de l'individu

id_resa = input("Entrez votre code de réservation :")
nom_voy =input("Entrez votre nom :")
prenom_voy = input("Entrez votre prenom :")

message = id_resa + ";" + nom_voy + ";" + prenom_voy + "\n"
socket_client.sendall(message.encode("utf-8"))

reponse = socket_client.recv(1024)
reponse = reponse.decode("utf-8").strip()

# vérification de la réponse et on répond au client en fonction du cas
if reponse.startswith("OK;"):
    print("Réservation  acceptée:",reponse)
else:
    if reponse == "REFUS;BILLET_INTROUVABLE":
        print("Billet introuvable")
    elif reponse == "REFUS;VOL_ANNULE":
        print("Votre vol est annulé")
    else:
        print("Réponse inattendue:",reponse)
    fermer_connexion()

#Vérification des bagages
poids_Soute = input("Veuillez renseigner le poid du bagage:")
message = poids_Soute + "\n"
socket_client.sendall(message.encode("utf-8"))

reponse = socket_client.recv(1024)
reponse = reponse.decode("utf-8").strip()

if reponse.startswith("OK;"):
    print("Voici votre numéro d'étiquette:", reponse)
else :
    print("Le bagage dépasse le poid limite autorisée, réduisez son poid ou payez le supplément de :",reponse)
    fermer_connexion()

long=input("Entrer la longueur :")
larg=input("Entrer la largeur :")
haut =input("Entrer la hauteur :")
message = long +";" +larg+";"+haut+"\n"

socket_client.sendall(message.encode("utf-8"))
reponse = socket_client.recv(1024)
reponse = reponse.decode("utf-8").strip()

if reponse == "REFUS;GABARIT_ELEVEE":
    print("Le bagage dépasse le gabarit autorisée")

message = "carte_embarquement"
socket_client.sendall(message.encode("utf-8"))
reponse = socket_client.recv(1024)
print("Voici votre carte d'embarquement :",reponse.decode("utf-8"))




