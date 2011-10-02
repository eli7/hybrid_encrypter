import java.util.Scanner;

public class UI {

    private int keyBits, ivBits;
    private byte[] cipherKey;    
    private byte[] mBlock;    
    private byte[] cBlock;
    private byte[] aData;
    private AEAD aead;
    private Scanner scanner;

    public UI() {
        this.scanner = new Scanner(System.in);
        this.aead = new LetterSoup();
        this.cipherKey = null;
        this.mBlock = null;
        this.cBLock = null;
        this.aData = null;
        this.ivBits = -1;
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
                    chooseIVSize();
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
                    System.out.print("Opção inválida. ");
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
            // Skip gibberish
            scanner.next();
            return -1;
        }
    }

    private void chooseCipherKeySize() {
        do {
            System.out.print("Digite um dos tamanhos de chave em bits disponíveis (96/144/192)\n$ ");
            this.keyBits = readInt();
        } while (this.keyBits == -1 || (this.keyBits != 96 && this.keyBits != 144 && this.keyBits != 192));
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
        }
        String password;
        // Read password
        do {
            System.out.print("Digite uma senha de até " + maxPasswordSize + " caracteres\n$ ");
            password = this.scanner.next();
        } while (password.length() > maxPasswordSize);
        // Right-pad password with zeros
        StringBuffer cipherKeyBuffer = new StringBuffer(password);
        while (cipherKeyBuffer.length() < maxPasswordSize) {
            cipherKeyBuffer.append("0");
        }
        // Save it as a byte array
        int keyBytes = this.keyBits / 8;
        this.cipherKey = new byte[keyBytes];
        for (int i = 0; i < keyBytes; i++) {
            this.cipherKey[i] = (byte)cipherKeyBuffer.charAt(i);
        }
    }       

    private void chooseIVSize() {
        do {
            System.out.print("Digite um tamanho de IV eentre 64 e 96 bits\n$ ");
            this.ivBits = readInt();
        } while (this.ivBits == -1 || this.ivBits < 96 || this.ivBits > 96);
    }

    private void authenticate() {
    }

    private void validate() {
    }

    private void encryptAndAuthenticate() {
    }

    private void validateAndDecrypt() {
    }

    private void encryptAndAuthenticateWithAssociatedData() {
    }

    private void validateAndDecryptWithAssociatedData() {
    }

    /** 
     * Reads a file and returns true upon success.
     */
    private boolean readFile(byte[] data) {
        System.out.print("Digite o nome do arquivo contendo a mensagem$ ");
        String fileName = this.scanner.next();
        File file = new File(fileName);
        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            data = new byte[(int)file.length()];
            fileInputStream.read(data);
        } catch(FileNotFoundException e) {
            System.out.println("Arquivo '" + fileName + "' não encontrado");
        } catch(IOException e) {
            System.out.println("Ocorreu uma exceção durante a leitura do arquivo: " + e);
            return false;
        }
        retun true;
    }
}

