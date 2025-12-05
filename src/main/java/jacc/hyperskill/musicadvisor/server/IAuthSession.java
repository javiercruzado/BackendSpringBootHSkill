package jacc.hyperskill.musicadvisor.server;

public interface IAuthSession {
    void getAccessToken(String code);
}
