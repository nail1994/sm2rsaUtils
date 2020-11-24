package com.gui;

import com.constants.InitEnum;
import com.service.RsaMessageService;
import com.util.CryptoUtils;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/****
 * @author  dwb
 * RSA GUI 加解密
 */
public class RsaGui extends JDialog
{
    public RsaGui(){

        this.setTitle("RSA加解密");
        Container container = this.getContentPane();

        //流式布局
        container.setLayout(new FlowLayout());

        JLabel privateLabel = new JLabel("RSA私钥:");
        JTextArea privateKey_area = new JTextArea(InitEnum.RSA_PRIVATEKEY.getValue(),5,3);

        JLabel publicLabel = new JLabel("RSA公钥:");
        JTextArea public_area = new JTextArea(InitEnum.RSA_PUBLICKEY.getValue(),5,3);

        JLabel mingwen = new JLabel("明文:");
        JTextArea mingwenArea = new JTextArea(5,3);

        JLabel miwen = new JLabel("密文:");
        JTextArea miwenArea = new JTextArea(5,3);


        JButton button = new JButton("加密");
        JButton button_2 = new JButton("解密");

        container.add(privateLabel);
        container.add(BorderLayout.CENTER,privateKey_area);
        container.add(publicLabel);
        container.add(BorderLayout.CENTER,public_area);
        container.add(mingwen);
        container.add(BorderLayout.CENTER,mingwenArea);
        container.add(miwen);
        container.add(BorderLayout.CENTER,miwenArea);

        container.add(button);
        container.add(button_2);

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("===加密===");
                try
                {
                    String publicKey = public_area.getText();
                    String temp = mingwenArea.getText();

                    RsaMessageService rsaMessageService = new RsaMessageService();
                    String s = CryptoUtils.byte2hex(org.apache.commons.codec.binary.Base64.decodeBase64(publicKey.getBytes()));
                    String resultStr = rsaMessageService.encryptHex(s, temp);

                    mingwenArea.setText("");
                    miwenArea.setText(resultStr);

                    System.out.println("===加密成功===");
                }catch (Exception e1){
                    e1.printStackTrace();
                    miwenArea.setText("加密失败!");
                }
            }
        });

        button_2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.out.println("===解密===start");
                try{

                    String privateKey = privateKey_area.getText();
                    String miwenStr = miwenArea.getText();

                    System.out.println("privateKey:"+privateKey);
                    System.out.println("miwenStr:"+miwenStr);

                    RsaMessageService service = new RsaMessageService();
                    String mingwenStr = service.rsaDecode(miwenStr,privateKey);
                    miwenArea.setText("");
                    mingwenArea.setText(mingwenStr);

                }catch (Exception e1){
                    e1.printStackTrace();
                    mingwenArea.setText("解密失败!");
                }

            }
        });

        this.setVisible(true);
        this.setSize(650,650);
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        this.setAlwaysOnTop(true);

    }

    public static void main(String[] args) {
        new RsaGui();
    }



}
