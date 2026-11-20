package com.redekrill.agendamentos.infrastructure.config;

import com.redekrill.agendamentos.domain.model.Perfil;
import com.redekrill.agendamentos.domain.model.Usuario;
import com.redekrill.agendamentos.domain.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/** Cria os usuarios iniciais (administrador e usuario comum) quando a base esta vazia. */
@Component
public class DataInitializer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataInitializer.class);

    private final UsuarioRepository usuarios;
    private final PasswordEncoder encoder;
    private final String adminEmail;
    private final String adminSenha;
    private final String comumEmail;
    private final String comumSenha;

    public DataInitializer(UsuarioRepository usuarios, PasswordEncoder encoder,
                           @Value("${app.seed.admin-email}") String adminEmail,
                           @Value("${app.seed.admin-senha}") String adminSenha,
                           @Value("${app.seed.comum-email}") String comumEmail,
                           @Value("${app.seed.comum-senha}") String comumSenha) {
        this.usuarios = usuarios;
        this.encoder = encoder;
        this.adminEmail = adminEmail;
        this.adminSenha = adminSenha;
        this.comumEmail = comumEmail;
        this.comumSenha = comumSenha;
    }

    @Override
    public void run(String... args) {
        if (usuarios.count() > 0) {
            return;
        }
        usuarios.save(new Usuario(null, "Administrador", adminEmail, encoder.encode(adminSenha), Perfil.ADMIN));
        usuarios.save(new Usuario(null, "Usuário Comum", comumEmail, encoder.encode(comumSenha), Perfil.COMUM));
        log.info("Usuários iniciais criados: {} (ADMIN) e {} (COMUM)", adminEmail, comumEmail);
    }
}
