package Serveur.GUI;

import Serveur.Controller.ControllerServeur;
import com.formdev.flatlaf.FlatLightLaf;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class ServeurWindow extends JFrame{
    private JPanel MainPanel;
    private JButton DemarrerButton;
    private JButton ArreterButton;
    private JPanel ButtonPanel;
    private JPanel LogPanel;
    private JScrollPane JscrollLog;
    private JTable tableLog;

    public JButton getDemarrerButton() {
        return DemarrerButton;
    }

    public JButton getArreterButton() {
        return ArreterButton;
    }

    public JTable getTableLog() {
        return tableLog;
    }

    public ServeurWindow(){
        setSize(600,300);
        setTitle("Serveur Java");
        setContentPane(MainPanel);
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        setLocationRelativeTo(null);

        tableLog = new JTable();
        DefaultTableModel tableModelEmp = (DefaultTableModel) tableLog.getModel();
        String[] nomsColonnes = { "Thread", "Action"};
        tableModelEmp.setColumnIdentifiers(nomsColonnes);
        JscrollLog.setViewportView(tableLog);
    }

    public void setControleur(ControllerServeur c)
    {
        DemarrerButton.addActionListener(c);
        ArreterButton.addActionListener(c);

        this.addWindowListener(c);
    }

    public static void main(String[] args)
    {
        FlatLightLaf.setup();

        ServeurWindow home = new ServeurWindow();
        home.setVisible(true);
    }
}
