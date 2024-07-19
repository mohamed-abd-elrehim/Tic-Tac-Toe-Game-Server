/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package xoserver;

/**
 *
 * @author COMPUMARTS
 */
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;

/**
 *
 * @author COMPUMARTS
 */
public class PlayerHandler extends Thread {

    DataInputStream dis;
    PrintStream ps;

    public PlayerHandler(Socket cs) {
        try {
            dis = new DataInputStream(cs.getInputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        try {
            ps = new PrintStream(cs.getOutputStream());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        start();
    }

    @Override
    public void run() {
        while (true) {
            String str;
            try {
                str = dis.readLine();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
    }
}
