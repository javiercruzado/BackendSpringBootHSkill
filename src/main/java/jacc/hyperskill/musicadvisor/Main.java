package jacc.hyperskill.musicadvisor;

import jacc.hyperskill.musicadvisor.server.HttpServerLocal;

import java.io.IOException;
import java.util.Objects;
import java.util.Scanner;

import static jacc.hyperskill.musicadvisor.server.InterfaceConstants.*;

public class Main {


    private static String accessToken;

    public static void main(String[] args) {

        System.out.println("Hello World!");

        String authUrl = APP_AUTH_URL + "/authorize";
        String tokenUrl;

        if (args.length >= 2 && !Objects.equals(args[1], "")) {
            authUrl = args[1] + "/authorize";
            tokenUrl = args[1] + "/api/token";
        } else {
            tokenUrl = APP_AUTH_URL + "/api/token";
        }

        String resourceUrl = APP_URL;
        if (args.length >= 4 && !Objects.equals(args[3], "")) {
            resourceUrl = args[3];
        }

        int pageSize = 5;
        if (args.length == 6 && !Objects.equals(args[5], "")) {
            try {
                pageSize = Integer.parseInt(args[5]);
            } catch (NumberFormatException ne) {
                System.out.printf("Exception %s", ne.getMessage());
            }
        }

        var controller = new Controller(pageSize, resourceUrl);

        try (Scanner sc = new Scanner(System.in)) {

            boolean exit = false;
            boolean authenticated = false;

            do {
                String userCommand = sc.nextLine();
                if (!authenticated &&
                        (userCommand.equals("new")
                                || userCommand.equals("featured")
                                || userCommand.equals("categories")
                                || userCommand.contains("playlists"))) {
                    System.out.println("Please, provide access for application.");
                    continue;
                }
                controller.setAccessToken(accessToken);

                if (authenticated) {
                    HttpServerLocal.stopLocalServer();
                }

                if (userCommand.equals("new")) {
                    int itemCount = controller.loadNewItems();
                    int numberOfPages = itemCount / pageSize;

                    int pageSelected = 1;
                    controller.getNewItems(pageSelected).forEach(i -> System.out.println(i.toString()));
                    printFooter(pageSelected, numberOfPages);

                    String pageCommand = sc.nextLine();
                    while (!"exit".equals(pageCommand)) {
                        int currentPage = pageSelected;
                        pageSelected = getNextPageToSelect(pageCommand, pageSelected, numberOfPages);
                        if (pageSelected != currentPage) {
                            controller.getNewItems(pageSelected).forEach(i -> System.out.println(i.toString()));
                            printFooter(pageSelected, numberOfPages);
                        }
                        pageCommand = sc.nextLine();
                    }

                } else if (userCommand.equals("featured")) {

                    int itemCount = controller.loadFeaturedItems();
                    int numberOfPages = itemCount / pageSize;

                    int pageSelected = 1;
                    controller.getFeaturedItems(pageSelected).forEach(i -> System.out.println(i.toString()));
                    printFooter(pageSelected, numberOfPages);

                    String pageCommand = sc.nextLine();
                    while (!"exit".equals(pageCommand)) {
                        int currentPage = pageSelected;
                        pageSelected = getNextPageToSelect(pageCommand, pageSelected, numberOfPages);
                        if (pageSelected != currentPage) {
                            controller.getFeaturedItems(pageSelected).forEach(i -> System.out.println(i.toString()));
                            printFooter(pageSelected, numberOfPages);
                        }
                        pageCommand = sc.nextLine();
                    }

                } else if (userCommand.equals("categories")) {
                    int itemCount = controller.loadCategories();
                    int numberOfPages = itemCount / pageSize;

                    int pageSelected = 1;
                    controller.getCategories(pageSelected).forEach(i -> System.out.println(i.name));
                    printFooter(pageSelected, numberOfPages);

                    String pageCommand = sc.nextLine();
                    while (!"exit".equals(pageCommand)) {
                        int currentPage = pageSelected;
                        pageSelected = getNextPageToSelect(pageCommand, pageSelected, numberOfPages);
                        if (pageSelected != currentPage) {
                            controller.getCategories(pageSelected).forEach(i -> System.out.println(i.name));
                            printFooter(pageSelected, numberOfPages);
                        }
                        pageCommand = sc.nextLine();
                    }

                } else if (userCommand.contains("playlists")) {
                    String category = userCommand.substring(10);
                    String categoryId = controller.getCategoryId(category);
                    if (category.isEmpty() || categoryId == null) {
                        categoryId = switch (category) {
                            case "Party Time" -> "party";
                            case "Super Mood" -> "mood";
                            case "Top Lists" -> "toplists";
                            default -> categoryId;
                        };
                    }

                    int itemCount = controller.loadPlayLists(categoryId);
                    int numberOfPages = itemCount / pageSize;

                    int pageSelected = 1;
                    controller.getPlayListItems(pageSelected).forEach(i -> System.out.println(i.toString()));
                    printFooter(pageSelected, numberOfPages);

                    String pageCommand = sc.nextLine();
                    while (!"exit".equals(pageCommand)) {
                        int currentPage = pageSelected;
                        pageSelected = getNextPageToSelect(pageCommand, pageSelected, numberOfPages);
                        if (pageSelected != currentPage) {
                            controller.getPlayListItems(pageSelected).forEach(i -> System.out.println(i.toString()));
                            printFooter(pageSelected, numberOfPages);
                        }
                        pageCommand = sc.nextLine();
                    }

                } else if (userCommand.equals("auth")) {
                    final boolean[] waitingForCode = {true};
                    try {
                        HttpServerLocal.startLocalServer(code -> {
                            System.out.println("code received");
                            System.out.println("making http request for access_token...");
                            waitingForCode[0] = false;

                            accessToken = Controller.getAuthToken(code, tokenUrl);
                        });
                    } catch (IOException e) {
                        System.out.printf("Exception %s", e.getMessage());
                    }
                    System.out.println("use this link to request the access code:");
                    String authLink = String.format("%s?client_id=%s&redirect_uri=%s&response_type=code",
                            authUrl,
                            CLIENT_ID,
                            APP_REDIRECT_URL);
                    System.out.println(authLink);
                    System.out.println("waiting for code...");

                    while (waitingForCode[0]) {
                        try {
                            Thread.sleep(1000);
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                    authenticated = true;
                } else if (userCommand.equals("exit")) {
                    System.out.println("---GOODBYE!---");
                    exit = true;
                    accessToken = "";
                    authenticated = false;
                } else {
                    System.out.println("Unknown command");
                }
            } while (!exit);
        }

        HttpServerLocal.stopLocalServer();

    }

    private static void printFooter(int page, int numberOfPages) {
        System.out.printf("---PAGE %d OF %d---%n", page, numberOfPages);
    }

    private static int getNextPageToSelect(String pageCommand, int page, int numberOfPages) {
        if ("next".equals(pageCommand)) {
            if (page == numberOfPages) {
                System.out.println("No more pages.");
                System.out.println();
            } else {
                page++;
            }
        } else if ("prev".equals(pageCommand)) {
            if (page == 1) {
                System.out.println("No more pages.");
                System.out.println();
            } else {
                page--;
            }
        } else {
            System.out.println("Please, use any of these [prev, next, exit]");
        }
        return page;
    }
}
