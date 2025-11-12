package com.keneyamuso.security;

import com.keneyamuso.model.entity.Utilisateur;
import com.keneyamuso.repository.UtilisateurRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

/**
 * Service pour charger les détails de l'utilisateur pour l'authentification
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UtilisateurRepository utilisateurRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String telephone) throws UsernameNotFoundException {
        Utilisateur utilisateur = utilisateurRepository.findByTelephone(telephone)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec le téléphone : " + telephone));

        List<GrantedAuthority> authorities = Collections.singletonList(
                new SimpleGrantedAuthority("ROLE_" + utilisateur.getRole().name())
        );

        return User.builder()
                .username(utilisateur.getTelephone())
                .password(utilisateur.getMotDePasse())
                .authorities(authorities)
                .accountExpired(false)
                .accountLocked(!utilisateur.getActif())
                .credentialsExpired(false)
                .disabled(!utilisateur.getActif())
                .build();
    }

    /**
     * Charge l'utilisateur complet par téléphone
     */
    @Transactional
    public Utilisateur loadUtilisateurByTelephone(String telephone) {
        return utilisateurRepository.findByTelephone(telephone)
                .orElseThrow(() -> new UsernameNotFoundException("Utilisateur non trouvé avec le téléphone : " + telephone));
    }
}

