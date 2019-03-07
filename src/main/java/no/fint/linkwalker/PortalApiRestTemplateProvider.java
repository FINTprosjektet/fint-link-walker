package no.fint.linkwalker;

import lombok.extern.slf4j.Slf4j;
import no.fint.oauth.OAuthRestTemplateFactory;
import no.fint.portal.model.client.Client;
import no.fint.portal.model.client.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.UUID;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ConcurrentSkipListMap;

@Service
@Slf4j
@ConditionalOnProperty(name = "fint.rest-template.provider", havingValue = "portal-api")
public class PortalApiRestTemplateProvider extends RestTemplateProvider {

    @Autowired
    private ClientService clientService;

    @Autowired
    private OAuthRestTemplateFactory oAuthRestTemplateFactory;

    private final ConcurrentMap<String, OAuth2RestTemplate> restTemplateCache = new ConcurrentSkipListMap<>();

    @PostConstruct
    public void init() {
        log.info("Authorization using Portal API enabled.");
    }

    @Override
    public RestTemplate getRestTemplate() {
        return withPermissiveErrorHandler(new RestTemplate());
    }

    @Override
    public RestTemplate getAuthRestTemplate(String client) {
        return withPermissiveErrorHandler(restTemplateCache.compute(client, this::computeOAuthRestTemplate));
    }

    private OAuth2RestTemplate computeOAuthRestTemplate(String client, OAuth2RestTemplate restTemplate) {
        if (restTemplate == null
                || restTemplate.getOAuth2ClientContext() == null
                || restTemplate.getOAuth2ClientContext().getAccessToken() == null
                || restTemplate.getOAuth2ClientContext().getAccessToken().isExpired()) {
            return createOAuthRestTemplate(client);
        }
        return restTemplate;
    }

    private OAuth2RestTemplate createOAuthRestTemplate(String clientDn) {
        Client client = clientService.getClientByDn(clientDn).orElseThrow(SecurityException::new);
        String password = UUID.randomUUID().toString().toLowerCase();
        clientService.resetClientPassword(client, password);
        String clientSecret = clientService.getClientSecret(client);

        return oAuthRestTemplateFactory.create(client.getName(), password, client.getClientId(), clientSecret);
    }

}