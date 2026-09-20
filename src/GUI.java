import javax.swing.*;
import java.awt.*;

public class GUI {
    public static void main(String[] args) {

        JFrame frame = new JFrame("Simple Java GUI");
        frame.setSize(400, 250);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setLayout(new FlowLayout());

        JLabel label = new JLabel("Enter your name:");

        JTextField textField = new JTextField(15);

        JButton button = new JButton("Submit");

        JLabel result = new JLabel("");

        button.addActionListener(e -> {
            String name = textField.getText();
            result.setText("Hello, " + name + "!");
        });

        frame.add(label);
        frame.add(textField);
        frame.add(button);
        frame.add(result);

        frame.setVisible(true);
    }
}
