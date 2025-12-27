package com.stackademy.proje.service;

import org.springframework.stereotype.Service;
import javax.naming.directory.Attributes;
import javax.naming.directory.InitialDirContext;
import javax.naming.NamingException;
import java.util.Hashtable;

@Service
public class EmailValidationService {

    /**
     * E-posta domain'inin geçerli bir MX (Mail Exchange) kaydı olup olmadığını
     * kontrol eder.
     * MX kaydı olan domainler gerçek mail alabilir, olmayanlar uydurma domain'dir.
     */
    public boolean isValidEmailDomain(String email) {
        if (email == null || !email.contains("@")) {
            return false;
        }

        String domain = email.substring(email.indexOf("@") + 1).toLowerCase().trim();

        if (domain.isEmpty()) {
            return false;
        }

        return hasMXRecord(domain);
    }

    /**
     * DNS sorgusu yaparak domain'in MX kaydı olup olmadığını kontrol eder.
     */
    private boolean hasMXRecord(String domain) {
        try {
            Hashtable<String, String> env = new Hashtable<>();
            env.put("java.naming.factory.initial", "com.sun.jndi.dns.DnsContextFactory");

            InitialDirContext ctx = new InitialDirContext(env);
            Attributes attrs = ctx.getAttributes(domain, new String[] { "MX" });
            ctx.close();

            // MX kaydı varsa domain geçerlidir
            return attrs.get("MX") != null && attrs.get("MX").size() > 0;

        } catch (NamingException e) {
            // DNS sorgusu başarısız - domain geçersiz veya bulunamadı
            return false;
        }
    }
}
