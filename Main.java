//Programmed by Irish Manalo & Ralph Vicente - March 2024

import java.sql.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.Scanner;
import java.sql.ResultSet;
import java.sql.PreparedStatement;

public class Main
{
    static String input;
    static Scanner sc = new Scanner(System.in);
    static Statement statement;
    static Connection connection;

    public static void main(String[] args)
    {
        try
        {
            Class.forName("com.mysql.jdbc.Driver");
            connection = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/adbmidterms", "root", "");
            statement = connection.createStatement();

            System.out.println("Welcome to Forum Application! Programmed by Althea Irish Manalo & Ralph Benedict Vicente");

            System.out.print("\n---What would you like to do?--- \n[A] Perform Existing Queries \n[B] CRUD \nInput: ");
            String input = sc.next();

            switch (input)
            {
                case "A":
                    existing();
                    break;
                case "B":
                    crud();
                    break;
                default:
                    throw new IllegalStateException("Invalid Input: " + input);
            }

            connection.close();
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    static void existing()
    {
        while (true)
        {
            try
            {
                System.out.print("---Choose from the existing procedure queries---");
                System.out.print("\n[A] Find a post using keyword/keyphrase \n[B] Saving and Updating post/comment \n[C] Deletion of a specific users' contents \n[D] Like Counts \n[E] Showing the comments and replies of a specific post \nInput: ");
                input = sc.next();

                switch (input) {
                    case "A":
                        A();
                        break;
                    case "B":
                        B();
                        break;
                    case "C":
                        C();
                        break;
                    case "D":
                        D();
                        break;
                    case "E":
                        E();
                        break;
                    default:
                        System.out.println("Invalid input. Please enter A, B, C, D, E");
                        continue;
                }

                System.out.print("\n\nDo you want to perform another operation? (Y/N): ");
                String choice = sc.next();
                if (choice.equalsIgnoreCase("N"))
                {   break;  }
            }
            catch (Exception e)
            {   System.out.println("Error: " + e.getMessage()); }
        }
    }


    //----------------------------------------------------------------------------FOR EXISTING QUERIES

    //----------------------------------------------------------------------------A
    static void A()
    {
        try
        {
            System.out.print("Enter keyword/keyphrase to search for: ");
            String keyword = sc.next();

            ResultSet resultSet = searchPostByKeyword(keyword);

            displayPosts(resultSet);
        }
        catch (SQLException e)
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static ResultSet searchPostByKeyword(String keyword) throws SQLException
    {
        String query = "SELECT * FROM Posts WHERE Content LIKE ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setString(1, "%" + keyword + "%");
        return preparedStatement.executeQuery();
    }

    static void displayPosts(ResultSet resultSet) throws SQLException
    {
        System.out.println("Search results:");
        while (resultSet.next())
        {
            int postId = resultSet.getInt("PostID");
            int userId = resultSet.getInt("UserID");
            int groupId = resultSet.getInt("GroupID");
            String content = resultSet.getString("Content");

            System.out.println("PostID: " + postId);
            System.out.println("UserID: " + userId);
            System.out.println("GroupID: " + groupId);
            System.out.println("Content: " + content);
            System.out.println();
        }
    }
    //----------------------------------------------------------------------------A

    //----------------------------------------------------------------------------B
    static void B() {
        try {
            System.out.print("Enter UserID: ");
            int userId = sc.nextInt();
            sc.nextLine();

            System.out.print("Enter GroupID: ");
            int groupId = sc.nextInt();
            sc.nextLine();

            System.out.print("Enter content: ");
            String content = sc.nextLine();

            if (postExists(connection, userId, groupId)) {
                updatePost(connection, userId, groupId, content);
                System.out.println("Post updated successfully!");
            } else {
                savePost(connection, userId, groupId, content);
                System.out.println("Post inserted successfully!");
            }
        } catch (SQLException e) {
            System.out.println("Error: " + e.getMessage());
        }
    }



    static boolean postExists(Connection connection, int userId, int groupId) throws SQLException {
        String query = "SELECT COUNT(*) FROM Posts WHERE UserID = ? AND GroupID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, groupId);
            try (ResultSet resultSet = statement.executeQuery()) {
                resultSet.next();
                return resultSet.getInt(1) > 0;
            }
        }
    }

    static void savePost(Connection connection, int userId, int groupId, String content) throws SQLException {
        String query = "INSERT INTO Posts (UserID, GroupID, Content) VALUES (?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, groupId);
            statement.setString(3, content);
            statement.executeUpdate();
        }
    }

    static void updatePost(Connection connection, int userId, int groupId, String content) throws SQLException {
        String query = "UPDATE Posts SET Content = ? WHERE UserID = ? AND GroupID = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, content);
            statement.setInt(2, userId);
            statement.setInt(3, groupId);
            statement.executeUpdate();
        }
    }
    //----------------------------------------------------------------------------B

    //----------------------------------------------------------------------------C


    static void C() {
        try {
            System.out.print("Enter the username to delete data: ");
            String username = sc.next();


            deleteUser(username);
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }


    static void deleteUser(String username) {
        try {
            int userId = getUserIdByUsername(username);

            if (userId != -1) {
                String deleteLikesQuery = "DELETE FROM Likes WHERE UserID = ?";
                PreparedStatement deleteLikesStatement = connection.prepareStatement(deleteLikesQuery);
                deleteLikesStatement.setInt(1, userId);
                deleteLikesStatement.executeUpdate();

                String deleteCommentsQuery = "DELETE FROM Posts WHERE UserID = ?";
                PreparedStatement deleteCommentsStatement = connection.prepareStatement(deleteCommentsQuery);
                deleteCommentsStatement.setInt(1, userId);
                deleteCommentsStatement.executeUpdate();

                String deletePostsQuery = "DELETE FROM Posts WHERE UserID = ?";
                PreparedStatement deletePostsStatement = connection.prepareStatement(deletePostsQuery);
                deletePostsStatement.setInt(1, userId);
                deletePostsStatement.executeUpdate();

                if (isUserModerator(userId)) {
                    removeModerator(userId);
                }

                String deleteUserQuery = "DELETE FROM Users WHERE UserID = ?";
                PreparedStatement deleteUserStatement = connection.prepareStatement(deleteUserQuery);
                deleteUserStatement.setInt(1, userId);
                deleteUserStatement.executeUpdate();

                System.out.println("User data deleted successfully.");
            } else {
                System.out.println("User not found.");
            }
        } catch (SQLException e) {
            System.out.println("Error deleting user data: " + e.getMessage());
        }
    }


    static int getUserIdByUsername(String username) {
        try {
            String query = "SELECT UserID FROM Users WHERE Username = ?";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, username);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt("UserID");
            } else {
                return -1;
            }
        } catch (SQLException e) {
            System.out.println("Error getting user ID by username: " + e.getMessage());
            return -1;
        }
    }


    static boolean isUserModerator(int userId) {
        try {
            String query = "SELECT * FROM User_Group WHERE UserID = ? AND Role = 'Moderator'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            ResultSet resultSet = preparedStatement.executeQuery();
            return resultSet.next(); // Return true if the user is a moderator
        } catch (SQLException e) {
            System.out.println("Error checking user moderator status: " + e.getMessage());
            return false;
        }
    }


    static void removeModerator(int userId) {
        try {
            String query = "DELETE FROM User_Group WHERE UserID = ? AND Role = 'Moderator'";
            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setInt(1, userId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error removing user's moderator role: " + e.getMessage());
        }
    }


    //----------------------------------------------------------------------------C


    //----------------------------------------------------------------------------D


    static void D() {
        try {
            System.out.print("Enter the PostID: ");
            int postId = sc.nextInt();
            int likeCount = getLikeCount(postId);
            System.out.println("Number of likes for PostID " + postId + ": " + likeCount);
        } catch (SQLException e) {
            System.out.println("Error counting likes for the post: " + e.getMessage());
        }
    }

    static int getLikeCount(int postId) throws SQLException {
        String query = "SELECT COUNT(*) AS LikeCount FROM Likes WHERE Activity = 'Post' AND ActivityID = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, postId);
        ResultSet resultSet = preparedStatement.executeQuery();
        if (resultSet.next()) {
            return resultSet.getInt("LikeCount");
        } else {
            return 0;
        }
    }


    //----------------------------------------------------------------------------D

    //----------------------------------------------------------------------------E

    static void E() {
        try {
            System.out.print("Enter the PostID: ");
            int postId = sc.nextInt();
            ResultSet resultSet = fetchCommentsAndReplies(postId);
            displayCommentsAndReplies(resultSet);
        } catch (SQLException e) {
            System.out.println("Error fetching comments and replies for the post: " + e.getMessage());
        }
    }

    static ResultSet fetchCommentsAndReplies(int postId) throws SQLException {
        String query = "SELECT * FROM Posts WHERE ParentPostID = ?";
        PreparedStatement preparedStatement = connection.prepareStatement(query);
        preparedStatement.setInt(1, postId);
        return preparedStatement.executeQuery();
    }

    static void displayCommentsAndReplies(ResultSet resultSet) throws SQLException {
        System.out.println("Comments and Replies:");
        while (resultSet.next()) {
            int commentId = resultSet.getInt("PostID");
            int userId = resultSet.getInt("UserID");
            String content = resultSet.getString("Content");
            String type = (resultSet.getInt("ParentPostID") == 0) ? "Comment" : "Reply";

            System.out.println("Comment/Reply ID: " + commentId);
            System.out.println("UserID: " + userId);
            System.out.println("Content: " + content);
            System.out.println("Type: " + type);
            System.out.println();
        }
    }


    //----------------------------------------------------------------------------E

    //-------------------------------------------------------------------------------------------CRUD
    public static void crud()
    {
        while (true)
        {
            try {
                System.out.print("---What would you like to perform?---");
                System.out.print("\n[C] Recursive Insertion \n[R] Read \n[U] Update \n[D] Recursive Deletion \nInput: ");
                input = sc.next();

                switch (input) {
                    case "C":
                        create();
                        break;
                    case "R":
                        read();
                        break;
                    case "U":
                        update();
                        break;
                    case "D":
                        delete();
                        break;
                    default:
                        System.out.println("Invalid input. Choose from the following: C, R, U, or D.");
                        continue;
                }

                System.out.print("Do you want to perform another? (Y/N): ");
                String choice = sc.next();
                if (choice.equalsIgnoreCase("N"))
                {   break;  }
            }
            catch (Exception e)
            {   System.out.println("Error: " + e.getMessage()); }
        }
    }

    public static void create() {
        try {
            System.out.print("=====CREATE=====");

            System.out.print("\nEnter UserID: ");
            int userId = sc.nextInt();

            System.out.print("Enter GroupID: ");
            int groupId = sc.nextInt();
            sc.nextLine();

            System.out.print("Enter ParentPostID (Optional, enter 0 if none): ");
            int parentPostId = sc.nextInt();
            sc.nextLine();

            System.out.print("Enter post content: ");
            String content = sc.nextLine();

            recursiveInsert(userId, groupId, parentPostId, content);

            System.out.println("Post inserted successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void recursiveInsert(int userId, int groupId, int parentPostId, String content) throws SQLException {
        // Insert the post
        String insertPostQuery = "INSERT INTO Posts (UserID, GroupID, Content, ParentPostID) VALUES (?, ?, ?, ?)";
        try (PreparedStatement insertPostStatement = connection.prepareStatement(insertPostQuery, Statement.RETURN_GENERATED_KEYS)) {
            insertPostStatement.setInt(1, userId);
            insertPostStatement.setInt(2, groupId);
            insertPostStatement.setString(3, content);
            insertPostStatement.setInt(4, parentPostId);
            insertPostStatement.executeUpdate();


            ResultSet generatedKeys = insertPostStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                int postId = generatedKeys.getInt(1);

                System.out.println("Post inserted with ID: " + postId);

                // If there are replies, recursively insert them
                if (content.startsWith("Reply to")) {
                    recursiveInsert(userId, groupId, postId, content);
                }
            }
        }
    }


    public static void read()
    {
        try
        {
            System.out.println("\n=====READ=====");

            String query = "SELECT Content FROM Posts";

            ResultSet resultSet = statement.executeQuery(query);

            System.out.println("Existing posts:");
            while (resultSet.next())
            {
                String postContent = resultSet.getString("Content");
                System.out.println(postContent);
            }

            resultSet.close();
        }
        catch (Exception e)
        {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void update()
    {
        try
        {
            System.out.println("\n=====UPDATE=====");

            System.out.print("Enter the PostID of the post you want to update: ");
            int postId = sc.nextInt();
            sc.nextLine();

            System.out.print("Enter the new content for the post: ");
            String newContent = sc.nextLine();

            String query = "UPDATE Posts SET Content = ? WHERE PostID = ?";

            PreparedStatement preparedStatement = connection.prepareStatement(query);
            preparedStatement.setString(1, newContent);
            preparedStatement.setInt(2, postId);

            int rowsAffected = preparedStatement.executeUpdate();

            if (rowsAffected > 0) {
                System.out.println("Post updated successfully!");
            } else {
                System.out.println("Failed to update post. The specified PostID may not exist.");
            }
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    public static void delete() {
        try {
            System.out.println("\n=====DELETE=====");

            System.out.print("Enter the PostID of the post you want to delete: ");
            int postId = sc.nextInt();

            recursiveDelete(postId);

            System.out.println("Post and associated comments/replies deleted successfully!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    static void recursiveDelete(int postId) throws SQLException {
        // Delete likes associated with the post
        String deleteLikesQuery = "DELETE FROM Likes WHERE Activity = 'Post' AND ActivityID = ?";
        try (PreparedStatement deleteLikesStatement = connection.prepareStatement(deleteLikesQuery)) {
            deleteLikesStatement.setInt(1, postId);
            deleteLikesStatement.executeUpdate();
        }

        String deleteCommentsQuery = "DELETE FROM Posts WHERE ParentPostID = ?";
        try (PreparedStatement deleteCommentsStatement = connection.prepareStatement(deleteCommentsQuery)) {
            deleteCommentsStatement.setInt(1, postId);
            deleteCommentsStatement.executeUpdate();
        }

        String selectRepliesQuery = "SELECT PostID FROM Posts WHERE ParentPostID = ?";
        try (PreparedStatement selectRepliesStatement = connection.prepareStatement(selectRepliesQuery)) {
            selectRepliesStatement.setInt(1, postId);
            ResultSet resultSet = selectRepliesStatement.executeQuery();
            while (resultSet.next()) {
                int replyId = resultSet.getInt("PostID");
                recursiveDelete(replyId);
            }
        }

        String deletePostQuery = "DELETE FROM Posts WHERE PostID = ?";
        try (PreparedStatement deletePostStatement = connection.prepareStatement(deletePostQuery)) {
            deletePostStatement.setInt(1, postId);
            deletePostStatement.executeUpdate();
        }
    }
}