import socket

adresseIP = "127.0.0.1"
port = 50000
#Fonction qui fait la fermeture de la connexion entre le serveur et client
def fermer_connexion():
    socket_client.sendall("FERMETURE\n".encode("utf-8"))
    reponse = socket_client.recv(1024).decode("utf-8").strip()
    print(reponse)
    socket_client.close()
    exit()

# on utilise cela car un message ne fait pas toujours 1024 octet
def recevoir_message():
    bmessage = b""

    while not bmessage.endswith(b"\n"):
        morceau = socket_client.recv(1)

        if not morceau:
            return ""

        bmessage += morceau

    return bmessage.decode("utf-8").strip()

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
enregistrement_valide = False
id_resa = input("Entrez votre code de réservation :")
nom_voy =input("Entrez votre nom :")
prenom_voy = input("Entrez votre prenom :")

message = id_resa + ";" + nom_voy + ";" + prenom_voy + "\n"
socket_client.sendall(message.encode("utf-8"))

reponse = socket_client.recv(1024)
reponse = reponse.decode("utf-8").strip()

# vérification de la réponse et on répond au client en fonction du cas
if reponse.startswith("OK;"):
    enregistrement_valide = True
    print("Réservation  acceptée:",reponse[3:])
else:
    if reponse == "REFUS;BILLET_INTROUVABLE":
        print("Billet introuvable")
    elif reponse == "REFUS;VOL_ANNULE":
        print("Votre vol est annulé")
    else:
        print("Réponse inattendue:",reponse)
    fermer_connexion()

#Vérification des bagages
choix_bagage = input("Avez vous un bagage en soute :(oui/non)")
if choix_bagage == "oui":
    poids_Soute = input("Veuillez renseigner le poid du bagage:")
    message = "SOUTE;"+poids_Soute + "\n"
    socket_client.sendall(message.encode("utf-8"))

    reponse = socket_client.recv(1024)
    reponse = reponse.decode("utf-8").strip()

    if reponse.startswith("OK;"):
        print("Voici votre numéro d'étiquette:", reponse[3:])
    else :
        print("Le bagage dépasse le poid limite autorisée, réduisez son poid ou payez le supplément de :",reponse)
        fermer_connexion()

choix_bagage = input("Avez vous un bagage en cabine :(oui/non)")

if choix_bagage == "oui":
    long=input("Entrer la longueur :")
    larg=input("Entrer la largeur :")
    haut =input("Entrer la hauteur :")
    message = "CABINE;"+long +";" +larg+";"+haut+"\n"

    socket_client.sendall(message.encode("utf-8"))
    reponse = socket_client.recv(1024)
    reponse = reponse.decode("utf-8").strip()

    if reponse == "OK":
        print("La valise respecte le gabarit ")

    elif reponse == "REFUS;GABARIT_ELEVEE":
        print("Le bagage dépasse le gabarit autorisé")
        fermer_connexion()
    else:
        print("Réponse inattendue :", reponse)
        fermer_connexion()
if enregistrement_valide:
    message = "carte_embarquement\n"
    socket_client.sendall(message.encode("utf-8"))
    reponse = socket_client.recv(1024)
    reponse = reponse.decode("utf-8").strip()
    infos = reponse.split(";")
    if len(infos) == 6 and infos[0] == "OK":
        print("===== CARTE D'EMBARQUEMENT =====")
        print("Vol :", infos[1])
        print("Nom :", infos[2])
        print("Prénom :", infos[3])
        print("Siège :", infos[4])
        print("Porte :", infos[5])

    else:
        print("Impossible de récupérer la carte d'embarquement")
    fermer_connexion()





