# FINT Link Walker

Verifies relation links for a given resource.

## Run with Docker Compose

Edit the supplied `docker-compose.yml` to add OAuth credentials, and use `docker-compose up` to start.

## Usage

Launching this application starts a web server at port `8080` with the following endpoints:

The `org` path parameter can be any value you choose.

| Path                          | Method | Description       |
|-------------------------------|--------|-------------------|
| `/api/tests/links/{org}`      | GET    | Get all tests     |
| `/api/tests/links/{org}`      | POST   | Start a new test, returns location header with direct url to the test  |
| `/api/tests/links/{org}/{id}` | GET    | Get test with id  |

## Model

The POST method requires a JSON object with the following elements:

    {
        "baseUrl": "https://api.felleskomponent.no",
        "endpoint": "/administrasjon/personal/personalressurs",
        "client": "client",
        "orgId": "pwf.no"
    }
    
| Element  | Description                         |
|----------|-------------------------------------|
| baseUrl  | Base URL for access.                |
| endpoint | Data endpoint to verify.            |
| client   | Some name for the client            |
| orgId    | Organization ID to verify data for. |

Base URL can be one of the following:
  - https://api.felleskomponent.no                   
  - https://beta.felleskomponent.no                  
  - https://play-with-fint.felleskomponent.no        

Endpoint refer to data elements, here are some examples:

  - `/administrasjon/personal/person`
  - `/administrasjon/personal/personalressurs`
  - `/utdanning/elev/person`
  - `/utdanning/elev/elev`

## OAuth 2.0 Environment variables

For protected resources, the following environment variables must be set with valid credentials:

| Variable                      | Content                                           |
|-------------------------------|---------------------------------------------------|
| `fint.oauth.enabled`          | Set to `true` to enable OAuth                     | 
| `fint.oauth.access-token-uri` | URI of access token server                        |
| `fint.oauth.scope`            | Set to `fint-client`                              |
| `fint.oauth.username`         | User Name                                         |
| `fint.oauth.password`         | Password                                          |
| `fint.oauth.client-id`        | OAuth Client ID                                   |
| `fint.oauth.client-secret`    | OAuth Client Secret                               |

Example access token server URI: https://idp.felleskomponent.no/nidp/oauth/nam/token
