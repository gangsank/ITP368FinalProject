package KeumMatching;

/*
 * Developed by Gangsan Keum.
 * This is a simple pokemon card matching game.
 * When the user opens the game, they can select a mode.
 * Once the user clicks a mode, they will enter the game.
 * The pokemon images will display for five seconds to memorize, then flip.
 * In double mode, user will have to pick a pair of matching cards.
 * In triple mode, user will have to pick a triplet of matching cards.
 * On the right side, the user can see how many pairs/triplets they have left to win
 * and the time elapsed since the start of the game.
 * Once the user reaches 0 pair/triplet left, they win.
 * The game will display for the best time at the top for each mode.
 * Clicking on the new game button at bottom will go back to select mode page.
 * Do not click the black rectangles consecutively way too quickly, 
 * as it can cause unexpected behaviors.
 * 
 * Special comment about keyboard mode:
 * I tried to let the user be able to play the game with keyboard
 * rather than just the mouse, but the way I designed the black rectangles and the images
 * stack in a stack pane caused a problem for keyboard mode.
 * Once the user clicks on a black rectangle to flip, it would lose the focus of that 
 * black rectangle once it gets flipped to an image, and it was a mess trying to
 * find the focus back and make keyboard feature work.
 * I spent a really long time, approx. 10 hrs, trying to make it work, but
 * it was getting out of hand at that point, so I consulted with professor, 
 * and he told me to just leave a comment about it.
 */

import java.util.ArrayList;
import java.util.Collections;
import javafx.animation.AnimationTimer;
import javafx.animation.ScaleTransition;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.*;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

public class KeumMatching extends Application
{
	Scene scene;
	VBox root, left, right;
	GridPane board;
	Button double_mode, triple_mode, new_game;
	boolean mode_is_double;
	int pairs_left; // how many pairs are left to match
	HBox title;
	ArrayList<Image> images_double; //images for double mode
	ArrayList<Image> images_triple; //images for triple mode
	Label pair, win, timerLabel; // label for pairs left, msg for win, time elapsed;
	Text time_score_double, time_score_triple; // best time for each mode
	long start, elapsed; //to calculate elapsed time
	long best_time_double=9999;
	long best_time_triple=9999; 
	
	//to check if clicked rects are the same images
	Rectangle current_image, last_image, last_last_image;
	Rectangle current_black, last_black, last_last_black;
	int click_count = 0;
	
	public static void main(String[] args) { launch (args); }
	
	public void start(Stage stage)
	{
		//usual first few lines
		root = new VBox();
		root.setBackground(new Background(new BackgroundFill(
				Color.FLORALWHITE, CornerRadii.EMPTY, Insets.EMPTY)));
		Scene scene = new Scene(root, 800, 600);
		stage.setTitle("KeumMatching");
		stage.setScene(scene);
		stage.show();
	
		//initialization of panes for layout
		left = new VBox();
		right = new VBox();
		board = new GridPane();
		title = new HBox();
		
		//initialize the arraylist of images
		images_double = new ArrayList<Image>();
		images_triple = new ArrayList<Image>();
		initializeImages();
		
		//creation of the mode selection page
		double_mode = new Button("Double");
		triple_mode = new Button("Triple");
		createStartPage();
		
		//display time from the start of new game
		timerLabel = new Label();
		timerLabel.setFocusTraversable(true);
        timerLabel.setFont(new Font("Verdana", 20.0));
        timerLabel.setTranslateY(150);
        Driver timer = new Driver();
        
        //event handlers for mode buttons
		double_mode.setOnAction( e ->
		{
			mode_is_double = true;
			root.getChildren().clear();
			createLayout();
			start = System.currentTimeMillis();
			timer.start();
		});
		
		triple_mode.setOnAction( e->
		{
			mode_is_double = false;
			root.getChildren().clear();
			createLayout();
			start = System.currentTimeMillis();
			timer.start();
		});
		
		stage.setOnCloseRequest(e -> {Platform.exit(); System.exit(0); } );
	}
	
	//counting elapsed time
	public class Driver extends AnimationTimer
	{
		@Override
        public void handle(long now) 
		{
            elapsed = System.currentTimeMillis() - start ;
            timerLabel.setText("Time: " + Long.toString(elapsed / 1000) + " sec");
            timerLabel.setAccessibleText(elapsed/1000 + "seconds has elapsed since the start");
        }
	}

	//create the start page i.e. select mode
	public void createStartPage()
	{
		try
		{
			//clear all previous children
			root.getChildren().clear();
			right.getChildren().clear();
			left.getChildren().clear();
			title.getChildren().clear();
			board.getChildren().clear();
			
			VBox beginning = new VBox();
			beginning.setTranslateY(300);
			
			//select mode
			Label label = new Label("Select a Mode");
			label.setFocusTraversable(true);
			label.setFont(new Font( "Verdana", 30.0));
			double_mode.setPrefSize(150, 50);
			triple_mode.setPrefSize(150, 50);
			double_mode.setFont(new Font("Verdana", 24.0));
			triple_mode.setFont(new Font("Verdana", 24.0));
			double_mode.setTranslateY(10);
			triple_mode.setTranslateY(20);
			beginning.setAlignment(Pos.CENTER);
			
			label.setAccessibleText("You are on a select mode page");
			double_mode.setAccessibleText("Click to enter double matching mode");
			triple_mode.setAccessibleText("Click to enter triple matching mode");
			
			beginning.getChildren().addAll(label, double_mode, triple_mode);
			root.getChildren().add(beginning);
		}
		catch(Exception e) {
			System.out.println("Could not create a start page!");
		}
	}
	
	//create appropriate layout of the game
	public void createLayout()
	{
		//shuffle cards for random assignment on the board
		Collections.shuffle(images_double);
		Collections.shuffle(images_triple);
		
		try
		{
			//double mode
			if(mode_is_double)
			{
				//adding title
				Label t = new Label("Match pairs!");
				t.setFont(new Font("Verdana", 30));
				t.setTranslateX(310);
				t.setTranslateY(10);
				
				//displaying best time for double mode
				Rectangle time = new Rectangle(220,50, Color.GRAY);
				if(best_time_double == 9999)
				{
					time_score_double = new Text("Best time: ");
				}
				else
				{
					time_score_double = new Text("Best time: " + best_time_double + " seconds");
				}
				time_score_double.setFont(new Font("Verdana", 18));
				time_score_double.setFill(Color.WHITE);
				time_score_double.setFocusTraversable(true);
				time_score_double.setAccessibleText("Current best time for double mode is " +  
						best_time_double);
				
				//stack the score rect and the time
				StackPane score = new StackPane();
				score.setTranslateX(370);
				score.setTranslateY(10);
				score.getChildren().addAll(time,time_score_double);
				
				title.getChildren().addAll(t, score);
				root.getChildren().add(title);
				
				//message for how pairs are left
				pairs_left = 18;
				pair = new Label("Pairs Left: " + Integer.toString(pairs_left));
				pair.setFocusTraversable(true);
				pair.setAccessibleText("You currently have " + pairs_left + " pairs left to match");
				pair.setFont(new Font("Verdana", 24.0));

				//grid pane for game board
				for(int i=0; i<6; i++)
				{ 
					if(i==3) Collections.shuffle(images_double);
					for (int j=0; j<6; j++)
					{
						//pull an image from the image arraylist and create a card
						Image image = images_double.remove(0);
						images_double.add(image);
						addCard(image, i, j);
					}
				}
			}
			
			//triple mode
			else
			{
				//adding title
				Label t = new Label("Match Triplets!");
				t.setFont(new Font("Verdana", 30));
				t.setTranslateX(280);
				t.setTranslateY(10);
				
				//displaying best time for double mode
				Rectangle time = new Rectangle(220,50, Color.GRAY);
				if(best_time_triple == 9999)
				{
					time_score_triple = new Text("Best time: ");
				}
				else
				{
					time_score_triple = new Text("Best time: " + best_time_triple + " seconds");
				}
				time_score_triple.setFont(new Font("Verdana", 18));
				time_score_triple.setFill(Color.WHITE);
				time_score_triple.setFocusTraversable(true);
				time_score_triple.setAccessibleText("Current best time for triple mode is " +  
						best_time_triple);
				
				//stack the score rect and the time
				StackPane score = new StackPane();
				score.setTranslateX(350);
				score.setTranslateY(10);
				score.getChildren().addAll(time,time_score_triple);
				
				title.getChildren().addAll(t, score);
				root.getChildren().add(title);
				
				//message for how many triplets are left
				pairs_left = 12;
				pair = new Label("Triplets Left: " + Integer.toString(pairs_left));
				pair.setFocusTraversable(true);
				pair.setAccessibleText("You currently have " + pairs_left + " triplets left to match");
				pair.setFont(new Font("Verdana", 24.0));
				
				//grid pane for game board
				for(int i=0; i<6; i++)
				{
					if(i==2 || i== 4)
					{
						Collections.shuffle(images_triple);
					}
					for (int j=0; j<6; j++)
					{
						//pull an image from the image arraylist and create a card
						Image image = images_triple.remove(0);
						images_triple.add(image);
						addCard(image, i, j);
					}
				}
			}
			
			board.setTranslateY(50);
			
			HBox bottom = new HBox();
			root.getChildren().add(bottom);
			bottom.getChildren().addAll(left,right);
			left.getChildren().add(board);
			left.setPrefSize(600, 500);
			
			//displaying information on the right side
			Button new_game = new Button("New Game");
			new_game.setPrefSize(120, 30);
			new_game.setFont(new Font("Verdana", 16.0));
			new_game.setTranslateY(350);
			new_game.setAccessibleText("Click to play a new game");
			
			//displaying message for when user wins the game i.e. finishes matching
			win = new Label("Keep going...");
			win.setFont(new Font("Verdana", 20.0));
			win.setFocusTraversable(true);
			win.setAccessibleText("Keep Going");
			win.setTranslateY(20);
			
			right.getChildren().addAll(pair,win,timerLabel,new_game);
			right.setTranslateY(55);
			
			//clicking on new game goes back to selecting mode
			new_game.setOnAction(e ->
			{
				createStartPage();
			});
		}
		catch(Exception e)
		{
			System.out.println("Could not create layout for the game!");
		}
	}
	
	//adding cards to the game board
	public void addCard(Image image, int i, int j)
	{
		try
		{
			//image card
			Rectangle img = new Rectangle(70,70);
			img.setTranslateX(i*10 + 10);
			img.setTranslateY(j*10 + 10);
			img.setFill(new ImagePattern(image));
			img.setId(image.getUrl()); //set the id of image to its file name
			img.setFocusTraversable(true);
			
			//a black rectangle that will be displayed when image is flipped
			Rectangle black = new Rectangle(70, 70);
			black.setScaleX(0);
			black.setTranslateX(i*10 + 10);
			black.setTranslateY(j*10 + 10);
			black.setFocusTraversable(true);
			black.setAccessibleText("You are on a black rectangle row "+ j + " and column " + i);
			 
			//Card Flip Animation
			ScaleTransition front = new ScaleTransition(Duration.millis(500), img);
			front.setFromX(1);
			front.setToX(0);
			front.setDelay(Duration.millis(5000));
			
			ScaleTransition back = new ScaleTransition(Duration.millis(500), black);
			back.setFromX(0);
			back.setToX(1);

			//stack the image and the black rectangle
			StackPane stack = new StackPane();
			stack.setPickOnBounds(false);
			stack.getChildren().addAll(img, black);
			board.add(stack, i, j);
			front.play();
			
			//when front finishes, play back
			front.setOnFinished(e->
			{
				back.play();
			});
			
			//event handler for clicking on the black rectangle i.e. flip to image
			if(mode_is_double)
			{
				black.addEventFilter(MouseEvent.MOUSE_CLICKED, e->flip_double(black,img));
			}
			else
			{
				black.addEventFilter(MouseEvent.MOUSE_CLICKED, e->flip_triple(black,img));
			}
		}
		catch(Exception e)
		{
			System.out.println("Could not add a card to the game board!");
		}
	}
	
	//flip card back from black to image for double mode
	public void flip_double(Rectangle black, Rectangle img)
	{
		try
		{
			click_count++;
			current_image = img;
			current_black = black;
			
			ScaleTransition b = new ScaleTransition(Duration.millis(500), black);
			b.setFromX(1);
			b.setToX(0);
			
			ScaleTransition f = new ScaleTransition(Duration.millis(500), img);
			f.setFromX(0);
			f.setToX(1);
			
			b.play();
			
			b.setOnFinished(e -> {
				f.play();
			});
			
			//if the two clicked images are not the same, flip them back
			if(click_count == 2 && current_image.getId() != last_image.getId())
			{
				//flip the two card back to black
				flipBack(current_black, current_image);
				flipBack(last_black, last_image);
				
				//reset click count to 0 and set the last to null
				click_count = 0;
				last_image = null;
				last_black = null;
			}
			
			//if the two clicked image are the same..
			else if(click_count == 2 && current_image.getId() == last_image.getId())
			{
				//reset click count to 0 and set the last to null
				click_count = 0;
				
				//set last to null
				last_image = null;
				last_black = null;
				
				//display how many pairs are left to match
				pairs_left--;
				pair.setText("Pairs Left: " + Integer.toString(pairs_left));
				pair.setAccessibleText("You currently have " + pairs_left + " pairs left to match");
				
				if(pairs_left == 0)
				{
					win.setText("You won!");
					
					//update best time
					if(elapsed/1000 < best_time_double)
					{
						best_time_double = elapsed/1000;
						time_score_double.setText("Best Time: " + best_time_double + " seconds");
					}
				}
			}
			
			//update the last 
			last_image = img;
			last_black = black;
		}
		catch(Exception e)
		{
			System.out.println("Could not flip a card for double mode!");
		}
	}
	
	//flip card back from black to image for triple mode
	public void flip_triple(Rectangle black, Rectangle img)
	{
		try
		{
			click_count++;
			current_image = img;
			current_black = black;
			
			ScaleTransition b = new ScaleTransition(Duration.millis(500), black);
			b.setFromX(1);
			b.setToX(0);
			
			ScaleTransition f = new ScaleTransition(Duration.millis(500), img);
			f.setFromX(0);
			f.setToX(1);
			
			b.play();
			
			b.setOnFinished(e -> {
				f.play();
			});
			
			//if the three clicked images are not the same, flip them back
			if(click_count == 3 && !(current_image.getId() == last_image.getId()
					&& last_image.getId() == last_last_image.getId()))
			{
				//flip the two card back to black
				flipBack(current_black, current_image);
				flipBack(last_black, last_image);
				flipBack(last_last_black, last_last_image);
				
				//reset click count to 0 and set the last to null
				click_count = 0;
				last_image = null;
				last_black = null;
				last_last_image = null;
				last_last_black = null;
			}
			
			//if the three clicked image are the same..
			else if(click_count == 3 && current_image.getId() == last_image.getId()
					&& last_image.getId() == last_last_image.getId())
			{
				//reset click count to 0 and set the last to null
				click_count = 0;
				
				//set last to null
				last_image = null;
				last_black = null;
				last_last_image = null;
				last_last_black = null;
				
				//display how many pairs are left to match
				pairs_left--;
				pair.setText("Triplets Left: " + Integer.toString(pairs_left));
				pair.setAccessibleText("You currently have " + pairs_left + " triplets left to match");
				
				if(pairs_left == 0)
				{
					win.setText("You won!");
					//update best time
					if(elapsed/1000 < best_time_triple)
					{
						best_time_triple = elapsed/1000;
						time_score_triple.setText("Best Time: " + best_time_triple + " seconds");
					}
				}
			}
			
			//update the last
			last_last_image = last_image;
			last_last_black = last_black;
			last_image = img;
			last_black = black;
		}
		catch(Exception e)
		{
			System.out.println("Could not flip a card for triple mode!");
		}
	}
	
	//flip card back from image to black
	public void flipBack(Rectangle black, Rectangle img)
	{
		try
		{
			ScaleTransition front = new ScaleTransition(Duration.millis(500), img);
			front.setFromX(1);
			front.setToX(0);
			front.setDelay(Duration.millis(800));
			
			ScaleTransition back = new ScaleTransition(Duration.millis(500), black);
			back.setFromX(0);
			back.setToX(1);
			
			front.play();
			
			//when front finishes, play back
			front.setOnFinished(e->
			{
				back.play();
			});
		}
		catch(Exception e)
		{
			System.out.println("Could not flip a card back to black!");
		}
	}
	
	//initialize the deck of pokemon cards
	public void initializeImages()
	{
		String[] address = {"Pokemons/Butterfree.png", "Pokemons/Caterpie.png",
				"Pokemons/Charmander.png", "Pokemons/Pidgey.png",
				"Pokemons/Pikachu.png", "Pokemons/Rock.png",
				"Pokemons/Sprout.png", "Pokemons/Squirtle.png",
				"Pokemons/Bulbasaur.png", "Pokemons/Oddish.png",
				"Pokemons/Paras.png", "Pokemons/Psyduck.png",
				"Pokemons/Growlithe.png", "Pokemons/Poliwag.png",
				"Pokemons/Abra.png", "Pokemons/Machop.png",
				"Pokemons/Ponyta.png", "Pokemons/Magnemite.png",  };
		
		//images for double mode
		for(int i=0; i<address.length; i++)
		{
			Image image = new Image(address[i]);
			images_double.add(image);
		}
		
		//images for triple mode
		for (int i=0; i<address.length-6; i++)
		{
			Image image = new Image(address[i]);
			images_triple.add(image);
		}
	}
}
