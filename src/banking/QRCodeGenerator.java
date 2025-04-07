package banking;

import io.nayuki.qrcodegen.QrCode;
import io.nayuki.qrcodegen.QrCode.Ecc;

public class QRCodeGenerator {
    // uses the library found at : https://github.com/nayuki/QR-Code-generator.
    public static void printQRCodeFromSecret(String username, String secret) {
        String data = "otpauth://totp/CSE237Bank:"+username+"?secret="+secret+"&issuer=CSE237Bank";
        QrCode qr = QrCode.encodeText(data, Ecc.MEDIUM);
        int border = 2;
        for (int y = -border; y < qr.size + border; y++) { // basic print loop to display the code
            for (int x = -border; x < qr.size + border; x++) {
                System.out.print(qr.getModule(x, y) ? "██" : "  ");
            }
            System.out.println();
        }
    }
}
