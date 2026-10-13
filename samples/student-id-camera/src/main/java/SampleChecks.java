import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.List;

import javax.imageio.ImageIO;

/** Real OCR smoke test on a synthetic text sheet, without opening the camera. */
final class SampleChecks {
    static void run() throws Exception {
        String[] schools = {"PUP", "NU MOA", "NU APC", "FEU TECH"};
        String[] numbers = {"2025-09011-MN-0", "2025-1058827", "2025-140065", "202511062"};
        BufferedImage sheet = new BufferedImage(1800, 1050, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = sheet.createGraphics();
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, sheet.getWidth(), sheet.getHeight());
        g.setColor(Color.BLACK);
        g.setFont(new Font("SansSerif", Font.BOLD, 38));
        g.drawString("STUDENT NUMBER OCR TEST — SAMPLE TEXT ONLY", 65, 75);
        g.setFont(new Font("SansSerif", Font.PLAIN, 46));
        for (int i = 0; i < numbers.length; i++) {
            List<StudentIdFormats.Match> found = StudentIdFormats.find(numbers[i]);
            check(
                    found.size() == 1 && found.get(0).school().equals(schools[i]),
                    "Parse " + schools[i]);
            g.drawString(schools[i], 65, 180 + i * 200);
            g.drawString(numbers[i], 65, 245 + i * 200);
        }
        g.dispose();
        check(
                StudentIdFormats.find("2025 - 09011 - MN - 0").get(0).number().equals(numbers[0]),
                "Spaced separators");
        check(
                StudentIdFormats.find("2025–140065").get(0).number().equals(numbers[2]),
                "Unicode separator");
        for (String invalid :
                new String[] {
                    "2025110620",
                    "1202511062",
                    "2025-1400659-8",
                    "2025-10588278",
                    "hello",
                    "2025-14O065"
                })
            check(
                    StudentIdFormats.find(invalid).isEmpty(),
                    "Reject invalid or ambiguous OCR: " + invalid);
        File output = new File("test-sheet.png");
        ImageIO.write(sheet, "png", output);
        String text = StudentIdCameraDemo.recognize(sheet);
        System.out.println("OCR test-sheet text:\n" + text);
        List<StudentIdFormats.Match> found = StudentIdFormats.find(text);
        for (int i = 0; i < numbers.length; i++)
            check(
                    found.contains(new StudentIdFormats.Match(schools[i], numbers[i])),
                    "OCR " + schools[i]);
        System.out.println(
                "PASS: all four number formats, invalid inputs, and real local OCR of all four"
                    + " samples.");
        System.out.println("Test sheet: " + output.getAbsolutePath());
    }

    private static void check(boolean condition, String message) {
        if (!condition) throw new AssertionError(message);
    }
}
