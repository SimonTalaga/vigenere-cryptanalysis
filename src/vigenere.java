import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class vigenere {

	public static int caesar_findGap(String filename) throws IOException
	{
		// Tableau des fréquences (Code ASCII, Fréquence)
		int frequences[] = new int[26];
		BufferedReader in = new BufferedReader(new FileReader(filename));
		String message = in.readLine();
						
		// Calcul des fréquences des lettres
		for(int i = 97; i <= 122; i++)
		{
			for(int j = 0; j < message.length(); j++)
			{
				if(message.charAt(j) == (char)i)
					frequences[i - 97]++;
			}
		}
				
		int highestIndex = 0;
		for(int i = 0; i < 26; i++)
		{
			if(frequences[i] > frequences[highestIndex])
				highestIndex = i;
		}
				
		System.out.println("\nLettre la plus fréquente : '" + (char)(highestIndex + 97) + "', correspond donc au 'e'");
		
		// On récupère la valeur du décalage
		int decalage = ((int)'z' - (int)'a') - ((((int)'z' - (int)'a') - highestIndex) + ((int)'e' - (int)'a'));
		if(decalage < 0)
			decalage += 26;
		System.out.println("Décalage trouvé : " + decalage);
		
		return decalage + 97;
	}
	
	public static String crypt(String text, String key)
	{
		StringBuffer sb = new StringBuffer(text);
		
		for(int i = 0; i < text.length(); i++)// 
		{
			// Décalage
			int decalage = (int)key.charAt(i % key.length()) - 97;
			int newCharCode = ((int)text.charAt(i) - 97 + decalage) % 26 + 97;
			sb.setCharAt(i, (char)newCharCode);
		}
		
		text = sb.toString();
		return text;
	}
	
	public static String decrypt(String text, String key)
	{
		StringBuffer sb = new StringBuffer(text);
		
		for(int i = 0; i < text.length(); i++)// 
		{
			int decalage = (int)key.charAt(i % key.length()) - 97; 
			
			int currentLetter = (int)text.charAt(i);
			if(currentLetter - decalage < 97)
				currentLetter += 26;
			
			int newCharCode = (currentLetter - 97 - decalage) % 26 + 97;
			sb.setCharAt(i, (char)newCharCode);
			//System.out.print(text.charAt(i) + " " + decalage + " " + (char)newCharCode + "\n");
		}
		
		text = sb.toString();
		return text;
	}
	
	public static String cryptanalysis(String ciphertext) throws IOException
	{
		// Cassage de la longueur de la clé en calculant l'indice de coincidence
		// On fait en force brute en testant pour chaque longueur de clé possible
		int I = 0;
		boolean unfound = true;
		ArrayList<String> createdFiles = new ArrayList<String>();
		
		// Pour chaque taille de clé possible, mettre jusqu'à la taille totale du texte
		while(unfound && I < ciphertext.length())
		{
			createdFiles.clear();
			I++;
			// Tableau où seront stocké les indices de coincidence
			float ic[] = new float[I];
			
			// Pour chaque "partition"
			for(int i = 0; i < I; i++)
			{
				// Pour une taille de clé, on divise le texte en sous textes qu'on stocke dans des fichiers
				String generatedFilename = "workingfiles/key" + I + "part" + i + ".txt"; 
				createdFiles.add(generatedFilename);
				
				FileWriter out = new FileWriter(generatedFilename, false);
				for(int j = 0; j < ciphertext.length(); j++)
				{
					if(j % I == i)
						out.write(ciphertext.charAt(j));
				}
				
				out.close(); // On vide le buffer
				
				// Pour chaque texte créé on stocke l'indice de coincidence calculé
				BufferedReader in = new BufferedReader(new FileReader(generatedFilename));
				String subtext = in.readLine();
								
				// Boucle pour calculer la fréquence
				for(int j = 97; j <= 122; j++)
				{
					int frequency = 0;
					
					for(int k = 0; k < subtext.length(); k++)
					{
						if(subtext.charAt(k) == (char)j)
							frequency++;
					}
					
					ic[i] += (float) (Math.pow(frequency, 2) / (Math.pow(subtext.length(), 2)));
				}

				in.close();
			}		
			
			// Taux d'erreur en %
			double tolerance = 15;
			boolean isGoodLength = true;
			System.out.println("Test pour la taille : " + I);
			
			for(int i = 0; i < ic.length; i++)
			{
				System.out.println((0.075 + (0.075 * tolerance / 100) + " " + ic[i] + " " + (0.075 - (0.075 * tolerance / 100))));
				if(0.075 + (0.075 * tolerance / 100) < ic[i] || 0.075 - (0.075 * tolerance / 100) > ic[i])
					isGoodLength = false;
			}
			
			// Si tous les indices correspondent, c'est certainement la bonne clé ! On la calcule et on la propose
			if(isGoodLength)
			{
				System.out.println("La clé est certainement de taille " + I);
				unfound = false;
			}
			
			// Si la taille de la clé ne parait pas être bonne, on continue
		}
		
		String key = "";
		for(int i = 0; i < createdFiles.size(); i++)
		{
			key += (char)caesar_findGap(createdFiles.get(i));
		}
		
		return key;
	}
	
	public static void main(String[] args) throws IOException {
		
		System.out.println("//////////////////////////////////////////////////////////");
		System.out.println("//              CHIFFREMENT DE VIGENERE                 //");
		System.out.println("//     (Chiffrement, Déchiffrement, Cryptanalyse)       //");
		System.out.println("//////////////////////////////////////////////////////////\n");
		
		Scanner sc = new Scanner(System.in);
		int option;
		String filename; 
		String key;
		
		// Menu
		System.out.println("Entrez le nom du fichier texte à traiter :\n");
		filename = sc.nextLine();
		File input = new File(filename);
		BufferedReader in = new BufferedReader(new FileReader(input));
		String text = in.readLine();
		String result = "";
		System.out.println("Entrez le numéro correspondant à l'action que vous voulez exécuter\n1 : Crypter\n2 : Décrypter\n3 : Attaquer\n");
		option = sc.nextInt();
		
		switch(option) 
		{
			case 1: 
				System.out.println("Entrez la clé de chiffrement :\n");
				key = sc.next();
				result = crypt(text, key);
				break;
				
			case 2:	
				System.out.println("Entrez la clé de chiffrement :\n");
				key = sc.next();
				result = decrypt(text, key);
				break;
				
			case 3:
				System.out.println("Calcul en cours...");
				System.out.println("La clé semble être : '" + cryptanalysis(text) + "'");
				System.exit(0);
				
			default:
				System.out.println("Vous n'avez pas entré de valeur valide.\n");
				System.exit(0);
		}
		
		System.out.println("Entrez le nom du fichier de sortie :");
		String outputfile = sc.next();
		FileWriter out = new FileWriter(outputfile, false);
		out.write(result);
		out.close();
	}
}