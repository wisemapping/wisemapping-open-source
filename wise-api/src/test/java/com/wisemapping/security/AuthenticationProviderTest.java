package com.wisemapping.security;

import com.wisemapping.exceptions.AccountDisabledException;
import com.wisemapping.model.Account;
import com.wisemapping.service.MetricsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationProviderTest {

    @Mock
    private UserDetailsService userDetailsService;
    
    @Mock
    private PasswordEncoder encoder;
    
    @Mock
    private MetricsService metricsService;
    
    @Mock
    private UserDetails userDetails;
    
    @Mock
    private Account account;

    private AuthenticationProvider authenticationProvider;

    @BeforeEach
    void setUp() {
        authenticationProvider = new AuthenticationProvider();
        authenticationProvider.setUserDetailsService(userDetailsService);
        authenticationProvider.setEncoder(encoder);
        authenticationProvider.setMetricsService(metricsService);
    }

    @Test
    void testAuthenticateDisabledAccount_ShouldThrowAccountDisabledException() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(email, password);

        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(account);
        when(encoder.matches(account.getPassword(), password)).thenReturn(true);
        when(account.isActive()).thenReturn(true);
        when(account.isSuspended()).thenReturn(true); // Account is suspended

        // When & Then
        AccountDisabledException exception = assertThrows(
            AccountDisabledException.class, 
            () -> authenticationProvider.authenticate(authToken)
        );

        assertEquals("ACCOUNT_SUSPENDED", exception.getMessage());
        verify(userDetailsService, never()).getUserService();
    }

    @Test
    void testAuthenticateActiveAccount_ShouldSucceed() {
        // Given
        String email = "test@example.com";
        String password = "password123";
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(email, password);

        when(userDetailsService.loadUserByUsername(email)).thenReturn(userDetails);
        when(userDetails.getUser()).thenReturn(account);
        when(encoder.matches(account.getPassword(), password)).thenReturn(true);
        when(account.isActive()).thenReturn(true);
        when(account.isSuspended()).thenReturn(false); // Account is not suspended
        when(userDetailsService.getUserService()).thenReturn(mock(com.wisemapping.service.UserService.class));
        when(userDetails.getAuthorities()).thenReturn(java.util.Collections.emptyList());

        // When
        var result = authenticationProvider.authenticate(authToken);

        // Then
        assertNotNull(result);
        assertTrue(result.isAuthenticated());
        verify(userDetailsService).getUserService();
        verify(metricsService).trackUserLogin(account, "database");
    }
}