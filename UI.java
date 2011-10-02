import java.util.Scanner;
import java.util.Arrays;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class UI {

    private int keyBits, ivBits, macTagBits;
    private byte[] cipherKey;  
    private AEAD aead;
    private BlockCipher blockCipher;
    private MAC mac;
    private Scanner scanner;

    public UI() {
        this.scanner = new Scanner(System.in);
        this.blockCipher = new Curupira();
        this.mac = new Marvin(true);
        this.aead = new LetterSoup();
        this.aead.setCipher(this.blockCipher);
        this.aead.setMAC(this.mac);
        this.cipherKey = null;
        this.macTagBits = 0;
    }

    public void start() {
        System.out.println("\n\n****************************************************");
        System.out.println("****************************************************");
        System.out.println(" CIFRADOR HÍBRIDO -> CURUPIRA + MARVIN & LETTERSOUP");
        System.out.println("****************************************************");
        System.out.println("****************************************************");
        printMenu();
        int option;
        do {
            System.out.print("Digite uma opção de 1 a 10 ou 0 para ver o menu.\n$ ");
            option = readInt();
            switch (option) {
                case 0:
                    printMenu();
                    break;
                case 1:
                    chooseCipherKeySize();
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
                    encryptAndAuthenticate();
                    break;
                case 6:
                    validateAndDecrypt();
                    break;
                case 7:
                    encryptAndAuthenticateWithAssociatedData();
                    break;
                case 8:
                    validateAndDecryptWithAssociatedData();
                    break;
                case 9:
                    System.exit(0);
                default:
                    System.out.print("ERRO: Opção inválida. ");
            }
        } while (true);
    }

    private void printMenu() {  
        System.out.println("----------------------------------------------------\n");        
        System.out.print
            ("[0]  Imprimir este menu novamente.\n\n"
             + "[1]  Selecionar um tamanho de chave dentre os valores admissíveis.\n"
             + "     e escolher uma senha alfanumérica (ASCII).\n\n"
             + "[2]  Selecionar um tamanho de IV e de MAC entre o mínimo de 64 bits\n"
             + "     e o tamanho completo do bloco.\n\n"
             + "[3]  Selecionar um arquivo para ser apenas autenticado.\n\n"
             + "[4]  Selecionar um arquivo com seu respectivo MAC para ser validado.\n\n"
             + "[5]  Selecionar um arquivo para ser cifrado e autenticado.\n\n"
             + "[6]  Selecionar um arquivo cifrado com seus respectivos IV e MAC para ser\n"
             + "     validado e decifrado.\n\n"
             + "[7]  Selecionar um arquivo para ser cifrado e autenticado, e um arquivo\n"
             + "     correspondente de dados associados para ser autenticado.\n\n"
             + "[8]  Selecionar um arquivo cifrado com seus respectivos IV e MAC para ser\n"
             + "     validado e decifrado, um arquivo correspondente de dados associados\n"
             + "     para ser autenticado.\n\n"
             + "[9] Encerrar programa.\n\n");
        System.out.println("----------------------------------------------------\n");                
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

    private void chooseCipherKeySize() {
        do {
            System.out.print(
                    "Digite um dos tamanhos de chave em bits disponíveis (96/144/192)\n$ ");
            this.keyBits = readInt();
        } while (this.keyBits == -1 || 
                (this.keyBits != 96 && this.keyBits != 144 && this.keyBits != 192));
        choosePassword();
    }

    private void choosePassword() {
        int maxPasswordSize;
        switch (this.keyBits) {
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
        String password;
        // Read password.
        do {
            System.out.print("Digite uma senha de até " + maxPasswordSize + " caracteres\n$ ");
            password = this.scanner.next();
        } while (password.length() > maxPasswordSize);
        // Right-pad password with zeros.
        StringBuffer cipherKeyBuffer = new StringBuffer(password);
        while (cipherKeyBuffer.length() < maxPasswordSize) {
            cipherKeyBuffer.append("0");
        }
        // Save it as a byte array.
        int keyBytes = this.keyBits / 8;
        this.cipherKey = new byte[keyBytes];
        for (int i = 0; i < keyBytes; i++) {
            this.cipherKey[i] = (byte)cipherKeyBuffer.charAt(i);
        }
    }

    private void chooseIVAndMACSize() {
        do {
            System.out.print(
                "Digite um dos tamanhos de IV em bits disponíveis (64/72/80/88/96).\n$ ");
            this.ivBits = readInt();
        } while (this.ivBits == -1 || 
                (this.ivBits != 64 && this.ivBits != 72 && this.ivBits != 80  &&
                 this.ivBits != 88 && this.ivBits != 96));
        // TODO check if IV and MAC may have different sizes.
        do {
            System.out.print(
                "Agora digite um dos tamanhos de MAC em bits disponíveis (64/72/80/88/96).\n$ ");
            this.macTagBits = readInt();
        } while (this.macTagBits == -1 || 
                (this.macTagBits != 64 && this.macTagBits != 72 && this.macTagBits != 80  &&
                 this.macTagBits != 88 && this.macTagBits != 96));        
    }

    private void authenticate() {
        // Check if a MAC size has been chosen.
        if (this.macTagBits == 0) {
            System.out.println("ERRO: escolha um tamanho de MAC (opção 2).");
            return;
        }
        // Read data file.
        byte[] aData = readFile("arquivo da mensagem");
        if (aData == null) return;
        // Authenticate data.
        this.mac.init();
        this.mac.update(aData, aData.length);
        byte[] macTag = this.mac.getTag(null, this.macTagBits);
        // Write MAC.
        if (!writeFile("arquivo para salvar o MAC", macTag)) return;
    }

    private void validate() {
        // Read message and MAC files.
        byte[] aData = readFile("arquivo da mensagem");
        byte[] macTag = readFile("arquivo do MAC");
        if (aData == null || macTag == null) return;
        // Calculate MAC from message.
        this.mac.init();
        this.mac.update(aData, aData.length);
        byte[] calculatedMacTag = this.mac.getTag(null, macTag.length * 8);
        // Check if MACs match.
        if (Arrays.equals(calculatedMacTag, macTag)) {
            System.out.println("A mensagem é válida.");
        } else {
            System.out.println("A mensagem é INVÁLIDA.");
        }
    }

    private void encryptAndAuthenticate() {
        // Check if an IV size has been chosen.
        if (this.ivBits == 0) {
            System.out.println("ERRO: escolha um tamanho de IV (opção 2).");
            return;
        }
        // Check if a password has been chosen.
        if (this.cipherKey == null) {
            System.out.println("ERRO: escolha uma senha (opção 1).");
            return;
        }
        // Check if a MAC size has been chosen.
        if (this.macTagBits == 0) {
            System.out.println("ERRO: escolha um tamanho de MAC (opção 2).");
            return;
        }        
        // Read message file.
        byte[] mData = readFile("arquivo da mensagem");
        if (mData == null) return;
        // Encrypt message and get MAC.
        // TODO setIV.
        this.aead.setKey(this.cipherKey, this.keyBits);
        byte[] cData = this.aead.encrypt(mData, mData.length, null);
        byte[] macTag = this.aead.getTag(null, this.macTagBits);
        // Write encrypted message and corresponding MAC.
        if (!writeFile("arquivo para salvar a mensagem cifrada", cData)) return;        
        if (!writeFile("arquivo para salvar o MAC", macTag)) return;
    }

    private void validateAndDecrypt() {
        // Check if a password has been chosen.
        if (this.cipherKey == null) {
            System.out.println("ERRO: escolha uma senha (opção 1).");
            return;
        }
        // Read encrypted message, MAC and IV files.
        byte[] cData = readFile("arquivo da mensagem cifrada");
        byte[] macTag = readFile("arquivo do MAC");
        byte[] iv = readFile("arquivo do IV");        
        if (cData == null || macTag == null || iv == null) return;        
        // Encrypt message and get MAC.
        this.aead.setIV(iv, iv.length);
        this.aead.setKey(this.cipherKey, this.keyBits);
        byte[] mData = this.aead.decrypt(cData, cData.length, null);
        // Validate MAC.
        byte[] calculatedMacTag = this.aead.getTag(null, macTag.length * 8);
        // Check if MACs match.
        if (Arrays.equals(calculatedMacTag, macTag)) {
            System.out.println("A mensagem é válida.");
        } else {
            System.out.println("A mensagem é INVÁLIDA.");
        }
        // Write decrypted message.
        if (!writeFile("arquivo para salvar a mensagem decifrada", mData)) return;
    }

    private void encryptAndAuthenticateWithAssociatedData() {
        // Read associated data file.
        byte[] aData = readFile("arquivo dos dados associados");
        if (aData == null) return;
        // Use associated data, encrypt and authenticate.
        this.aead.update(aData, aData.length);
        encryptAndAuthenticate();
    }

    private void validateAndDecryptWithAssociatedData() {
        // Read associated data file.
        byte[] aData = readFile("arquivo dos dados associados");
        if (aData == null) return;
        // Use associated data, decrypt and validate.
        this.aead.update(aData, aData.length);
        validateAndDecrypt();        
    }

    /**
     * Asks a a file name and reads it.
     * @return  in case of success, a byte array containing the read data
     *          in case of failure, null.
     */
    private byte[] readFile(String fileDescription) {
        byte[] data = null;
        System.out.print("Digite o nome do " + fileDescription + ".\n$ ");
        String fileName = this.scanner.next();
        File file = new File(fileName);
        FileInputStream fileInputStream = null;
        try {
            fileInputStream = new FileInputStream(file);
            data = new byte[(int)file.length()];
            fileInputStream.read(data);
            fileInputStream.close();
        } catch(FileNotFoundException e) {
            System.out.println("ERRO: arquivo '" + fileName + "' não encontrado");
        } catch(IOException e) {
            System.out.println("ERRO: ocorreu uma exceção durante a leitura do arquivo: " + e);
        }
        return data;
    }

    /**
     * Asks a name for the new file and writes data to it.
     * @param   data    a byte array containing data to be written.
     * @return          true in case of success and false otherwise.
     *
     */
    private boolean writeFile(String fileDescription, byte[] data) {
        System.out.print("Digite o nome de um " + fileDescription + ".\n$ ");
        String fileName = this.scanner.next();
        File file = new File(fileName);
        FileOutputStream fileOutputStream = null;
        try {
            fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(data);
            fileOutputStream.close();
        } catch (IOException e) {
            System.out.println("ERRO: ocorreu uma exceção durante a escrita do arquivo: " + e);
            return false;
        }
        return true;
    }
}

