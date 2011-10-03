package core;

import interfaces.AEAD;
import interfaces.BlockCipher;
import interfaces.MAC;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Random;
import java.util.Scanner;

public class UI {

    private int keyBits, ivBits, macTagBits;
    private byte[] cipherKey;
    private AEAD aead;
    private MAC mac;
    private Scanner scanner;
    private Random random;

    public UI() {
	scanner = new Scanner(System.in);
	random = new Random(System.currentTimeMillis());
	BlockCipher blockCipher = new Curupira1();
	mac = new Marvin(false);
	mac.setCipher(blockCipher);
	aead = new LetterSoup();
	aead.setMAC(new Marvin(true));
	aead.setCipher(blockCipher);
	cipherKey = null;

	// Default values
	keyBits = 96;
	ivBits = 96;
	macTagBits = 96;
	cipherKey = new byte[12];
    }

    public void start() {
	System.out
		.println("\n* ------------------------------------------------------------------- *");
	System.out
		.println("*                                                                     *");
	System.out
		.println("*         CIFRADOR HIBRIDO --> CURUPIRA + MARVIN & LETTERSOUP         *");
	System.out
		.println("*                                                                     *");
	System.out
		.println("* ------------------------------------------------------------------- *\n");
	printMenu();

	while (true) {
	    System.out
		    .print("Digite uma opcao de 1 a 10 ou 0 para ver o menu.\n$ ");
	    switch (readInt()) {
	    case 0:
		printMenu();
		break;
	    case 1:
		chooseCipherKeySize();
		choosePassword();
		break;
	    case 2:
		chooseIVAndMACSize();
		break;
	    case 3:
		authenticate();
		break;
	    case 4:
		validate();
		break;
	    case 5:
		encryptAndAuthenticate(false);
		break;
	    case 6:
		validateAndDecrypt(false);
		break;
	    case 7:
		encryptAndAuthenticate(true);
		break;
	    case 8:
		validateAndDecrypt(true);
		break;
	    case 9:
		System.exit(0);
	    default:
		System.out.print("ERRO: Opcao invalida. ");
	    }
	}
    }

    private void printMenu() {
	System.out
		.println("* ------------------------------------------------------------------- *\n");
	System.out
		.print("[0]  Imprimir este menu novamente.\n"
			+ "[1]  Selecionar um tamanho de chave e escolher uma senha\n"
			+ "     alfanumerica (ASCII).\n"
			+ "[2]  Selecionar tamanhos de IV e de MAC.\n"
			+ "[3]  Autenticar um arquivo.\n"
			+ "[4]  Validar um arquivo com base em seu MAC.\n"
			+ "[5]  Cifrar e autenticar um arquivo.\n"
			+ "[6]  Validar e decifrar um arquivo com base em seus valores de IV e MAC.\n"
			+ "[7]  Cifrar um arquivo e autentica-lo juntamente com um arquivo de dados\n"
			+ "     associados correspondente.\n"
			+ "[8]  Decifrar um arquivo e valida-lo juntamente com um arquivo de dados\n"
			+ "     associado com base em seus valores de IV e MAC.\n"
			+ "[9]  Encerrar programa.\n\n"
			+ "Valores 'default': chave, IV e MAC de 96 bits, chave 0x000...0\n\n");
	System.out
		.println("* ------------------------------------------------------------------- *\n");
    }

    private int readInt() {
	if (scanner.hasNextInt()) {
	    return scanner.nextInt();
	} else {
	    // Skip gibberish.
	    scanner.next();
	    return -1;
	}
    }

    // Menu - Option 1 (Part 1)
    private void chooseCipherKeySize() {
	do {
	    System.out
		    .print("Digite um dos tamanhos de chave em bits disponiveis (96/144/192)\n$ ");
	    keyBits = readInt();
	} while (keyBits == -1
		|| (keyBits != 96 && keyBits != 144 && keyBits != 192));
    }

    // Menu - Option 1 (Part 2)
    private void choosePassword() {
	int maxPasswordSize;
	switch (keyBits) {
	case 96:
	    maxPasswordSize = 12;
	    break;
	case 144:
	    maxPasswordSize = 18;
	    break;
	case 192:
	    maxPasswordSize = 24;
	    break;
	default:
	    throw new RuntimeException("An invalid key size was selected.");
	}
	// Read password.
	String password;
	do {
	    System.out.print("Digite uma senha de ate' " + maxPasswordSize
		    + " caracteres\n$ ");
	    password = scanner.next();
	} while (password.length() > maxPasswordSize);
	// Right-pad password with zeros.
	StringBuffer cipherKeyBuffer = new StringBuffer(password);
	while (cipherKeyBuffer.length() < maxPasswordSize)
	    cipherKeyBuffer.append("0");
	// Save it as a byte array.
	int keyBytes = keyBits / 8;
	cipherKey = new byte[keyBytes];
	for (int i = 0; i < keyBytes; i++)
	    cipherKey[i] = (byte) cipherKeyBuffer.charAt(i);
    }

    // Menu - Option 2
    private void chooseIVAndMACSize() {
	do {
	    System.out
		    .print("Digite um dos tamanhos disponiveis de IV em bits (64/72/80/88/96).\n$ ");
	    ivBits = readInt();
	} while (ivBits == -1
		|| (ivBits != 64 && ivBits != 72 && ivBits != 80
			&& ivBits != 88 && ivBits != 96));
	do {
	    System.out
		    .print("Agora digite um dos tamanhos disponiveis de MAC em bits (64/72/80/88/96).\n$ ");
	    macTagBits = readInt();
	} while (macTagBits == -1
		|| (macTagBits != 64 && macTagBits != 72 && macTagBits != 80
			&& macTagBits != 88 && macTagBits != 96));
    }

    // Menu - Option 3
    private void authenticate() {
	// Read data file.
	String fileName = getFileName("arquivo da mensagem", true);
	byte[] aData = readFile(fileName);
	if (aData == null)
	    return;
	// Authenticate data.
	mac.setKey(cipherKey, keyBits);
	mac.init();
	mac.update(aData, aData.length);
	byte[] macTag = mac.getTag(null, macTagBits);
	// Write MAC.
	if (!writeFile(macTag, fileName + ".mac"))
	    return;
    }

    // Menu - Option 4
    private void validate() {
	// Read message and MAC files.
	byte[] aData = readFile(getFileName("arquivo da mensagem", true));
	byte[] macTag = readFile(getFileName("arquivo de MAC", true));
	if (aData == null || macTag == null)
	    return;
	// Calculate MAC from message.
	mac.setKey(cipherKey, keyBits);
	mac.init();
	mac.update(aData, aData.length);
	byte[] calculatedMacTag = mac.getTag(null, macTag.length * 8);
	// Check if MACs match.
	if (Arrays.equals(calculatedMacTag, macTag))
	    System.out.println("A mensagem e' valida.");
	else
	    System.out.println("A mensagem e' INVALIDA.");
    }

    // Menu - Options 5/7
    private void encryptAndAuthenticate(boolean hasAssociatedData) {
	// Read message file.
	String msgFileName = getFileName("arquivo da mensagem", true);
	byte[] mData = readFile(msgFileName);
	if (mData == null)
	    return;
	// Read associated data file.
	byte[] aData = null;
	if (hasAssociatedData) {
	    aData = readFile(getFileName("arquivo dos dados associados", true));
	    if (aData == null)
		return;
	}
	// Encrypt message and get MAC.
	byte[] iv = generateIV();
	aead.setKey(cipherKey, keyBits);
	aead.setIV(iv, ivBits / 8);
	if (hasAssociatedData)
	    aead.update(aData, aData.length);
	byte[] cData = aead.encrypt(mData, mData.length, null);
	byte[] macTag = aead.getTag(null, macTagBits);
	// Write encrypted message and corresponding MAC and IV.
	if (!writeFile(cData, msgFileName + ".ciph"))
	    return;
	if (!writeFile(macTag, msgFileName + ".mac"))
	    return;
	if (!writeFile(iv, msgFileName + ".iv"))
	    return;
    }

    // Menu - Option 6/8
    private void validateAndDecrypt(boolean hasAssociatedData) {
	// Read encrypted message, MAC and IV files.
	byte[] cData = readFile(getFileName("arquivo da mensagem cifrada", true));
	byte[] macTag = readFile(getFileName("arquivo de MAC", true));
	byte[] iv = readFile(getFileName("arquivo do IV", true));
	if (cData == null || macTag == null || iv == null)
	    return;
	// Read associated data file.
	byte[] aData = null;
	if (hasAssociatedData) {
	    aData = readFile(getFileName("arquivo dos dados associados", true));
	    if (aData == null)
		return;
	}
	// Decrypt message and get MAC.
	aead.setKey(cipherKey, keyBits);
	aead.setIV(iv, iv.length);
	byte[] mData = aead.decrypt(cData, cData.length, null);
	if (hasAssociatedData)
	    aead.update(aData, aData.length);
	aead.encrypt(mData, mData.length, null);
	byte[] calculatedMacTag = aead.getTag(null, macTag.length * 8);
	// Check if MACs match.
	if (Arrays.equals(calculatedMacTag, macTag))
	    System.out.println("A mensagem e' valida.");
	else
	    System.out.println("A mensagem e' INVALIDA.");
	// Write decrypted message.
	if (!writeFile(
		mData,
		getFileName("arquivo onde sera salva a mensagem decifrada",
			false)))
	    return;
    }

    /**
     * Asks the user to input a file name. If the {@code checkExistence} flag is
     * {@code true} then the method keeps asking for input until the user
     * chooses the name of a file that already exists.
     * 
     * @param fileDescription
     *            A description of the file, to be used in the messages to the
     *            user.
     * @param checkExistence
     *            Indicates whether the method should accept only names of files
     *            that already exist.
     * @return The name of the chosen file.
     */
    private String getFileName(String fileDescription, boolean checkExistence) {
	String fileName = null;
	do {
	    System.out.print("Digite o nome do " + fileDescription + ".\n$ ");
	    do
		fileName = scanner.nextLine().trim();
	    while (fileName.length() == 0);

	    if (checkExistence && !(new File(fileName).exists())) {
		System.out.println("ERRO: arquivo '" + fileName
			+ "' nao encontrado");
		fileName = null;
	    }
	} while (fileName == null);

	return fileName;
    }

    /**
     * Reads the data of a file and puts it in a byte array.
     * 
     * @param fileName
     *            The name of the file to be read.
     * @return The data of the file.
     */
    private byte[] readFile(String fileName) {
	byte[] data = null;
	try {
	    FileInputStream fileInputStream = new FileInputStream(fileName);
	    data = new byte[(int) new File(fileName).length()];
	    fileInputStream.read(data);
	    fileInputStream.close();
	} catch (IOException e) {
	    System.out
		    .println("ERRO: ocorreu um erro durante a leitura do arquivo: '"
			    + fileName + "'");
	}
	return data;
    }

    /**
     * Writes data to a file. Overwrites if the file already exists.
     * 
     * @param data
     *            The data to be written.
     * @param fileName
     *            The name of the file where the data will be written.
     * @return {@code true} in case of success and {@code false} otherwise.
     */
    private boolean writeFile(byte[] data, String fileName) {
	try {
	    FileOutputStream fileOutputStream = new FileOutputStream(fileName);
	    fileOutputStream.write(data);
	    fileOutputStream.close();
	} catch (IOException e) {
	    System.out
		    .println("ERRO: ocorreu um erro durante a escrita do arquivo: '"
			    + fileName + "'");
	    return false;
	}
	return true;
    }

    /**
     * Generates a random initialization vector.
     * 
     * @return the generated vector.
     */
    private byte[] generateIV() {
	byte[] iv = new byte[ivBits / 8];
	random.nextBytes(iv);
	return iv;
    }
}
