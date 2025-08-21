package org.shuzhi.Service;

import com.sun.tools.jconsole.JConsoleContext;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class EmailService {
    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private RedisTemplate redisTemplate;

    private final String FROM_EMAIL = "1428767439@qq.com";

    private final String SENT_TITLE = "您现在正在登录DB-MASTER";

    private final String SENT_CONTENT = "你好，您现在正在登录DB-MASTER,请在登录页输入以下验证码: %s ,关系到的您的信息安全，请勿将验证码发给他人";

    // 发送简单邮件

    /**
     * 发送邮件
     * @param email
     */
    public void sendSimpleMail(String email) {

        SimpleMailMessage message = new SimpleMailMessage();
        String verificationCode = generateVerificationCode();
        message.setFrom(FROM_EMAIL);
        message.setTo(email);
        message.setSubject(SENT_TITLE);
        message.setText(String.format(SENT_CONTENT, verificationCode));
        // 暂时注销
//        try {
//            mailSender.send(message);
//        } catch (Exception e) {
//            throw new RuntimeException("发送邮件失败");
//        }
        try {
            redisTemplate.opsForSet().add(email, verificationCode);
            redisTemplate.expire(email, 5, TimeUnit.MINUTES);
            System.out.println("动态密码保存成功 ==>> " + email + " " + verificationCode);
        } catch (Exception e) {
            System.out.println(e);
            throw new RuntimeException("保存验证码失败");
        }
    }

    /**
     * 生成六位验证码，包括数字和大写字母，结合当前时间戳
     * @return 六位验证码
     */
    public String generateVerificationCode() {
        // 使用当前时间戳作为随机数种子的一部分
        long timestamp = System.currentTimeMillis();
        Random random = new Random(timestamp);
        
        // 定义验证码字符集，包括数字和大写字母
        String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        StringBuilder code = new StringBuilder();
        
        // 生成6位随机验证码
        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(chars.length());
            code.append(chars.charAt(index));
        }
        
        return code.toString();
    }
}
