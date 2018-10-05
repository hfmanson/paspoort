
package nl.mansoft.paspoort;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.smartcardio.CardException;
import javax.smartcardio.CardTerminal;
import javax.smartcardio.CardTerminals;
import javax.smartcardio.TerminalFactory;
import javax.xml.bind.DatatypeConverter;
import net.sf.scuba.smartcards.CardService;
import net.sf.scuba.smartcards.CardServiceException;
import org.jmrtd.BACKey;
import org.jmrtd.PassportService;
import org.jmrtd.lds.LDSFileUtil;
import org.jmrtd.lds.icao.DG1File;

import static org.jmrtd.PassportService.DEFAULT_MAX_BLOCKSIZE;
import static org.jmrtd.PassportService.NORMAL_MAX_TRANCEIVE_LENGTH;
import static org.jmrtd.PassportService.EF_DG1;

public class Paspoort {
    public static void main(String[] args) {
        if (args.length != 3) {
            System.err.println("Usage:\t" + Paspoort.class.getName() + " <document number> <date of birth> <expiry date>\n\tDate format: YYMMDD");
            System.exit(1);
        }
        try {
            String documentNumber = args[0];
            String dateOfBirth = args[1];
            String dateOfExpiry = args[2];
            BACKey key = new BACKey(documentNumber, dateOfBirth, dateOfExpiry);
            System.out.println("BAC key: " + DatatypeConverter.printHexBinary(key.getKey()));
            List<CardTerminal> terminals = TerminalFactory.getDefault().terminals().list();
            if (!terminals.isEmpty()) {
                CardTerminal ct = terminals.get(0);
                CardService cs = CardService.getInstance(ct);
                PassportService ps = new PassportService(cs, NORMAL_MAX_TRANCEIVE_LENGTH, DEFAULT_MAX_BLOCKSIZE, false, false);
                ps.open();
                // select A0000002471001 -- Biometric Passpord
                ps.sendSelectApplet(false);
                // perform Basic Access Control
                ps.doBAC(key);
                InputStream is = ps.getInputStream(EF_DG1);
                DG1File dg1 =  (DG1File) LDSFileUtil.getLDSFile(EF_DG1, is);
                System.out.println(dg1.getMRZInfo().getPersonalNumber());
            } else {
                System.err.println("No terminal");
            }
        } catch (Exception ex) {
            System.err.println("@@" + ex.getMessage());  
        }
    }
    
}
