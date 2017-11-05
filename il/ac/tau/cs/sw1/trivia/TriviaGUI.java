package il.ac.tau.cs.sw1.trivia;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class TriviaGUI {

	private static final int MAX_ERRORS = 3;
	private Shell shell;
	private Label scoreLabel;
	private Composite questionPanel;
	private Font boldFont;
	private String lastAnswer = "";
	
	private Integer currentScore;
	private int numOfWrongAnswers;
	private int questionCnt;
	private List<List<List<String>>> triviaList; 
	private int triviaListIndex;
	
	
	public void open() {
		createShell();
		runApplication();
	}

	/**
	 * Creates the widgets of the application main window
	 */
	private void createShell() {
		Display display = Display.getDefault();
		shell = new Shell(display);
		shell.setText("Trivia");

		// window style
		Rectangle monitor_bounds = shell.getMonitor().getBounds();
		shell.setSize(new Point(monitor_bounds.width / 3,
				monitor_bounds.height / 4));
		shell.setLayout(new GridLayout());

		FontData fontData = new FontData();
		fontData.setStyle(SWT.BOLD);
		boldFont = new Font(shell.getDisplay(), fontData);
		
		//create window panels
		createFileLoadingPanel();
		createScorePanel();
		createQuestionPanel();
	}

	/**
	 * Creates the widgets of the form for trivia file selection
	 */
	private void createFileLoadingPanel() {
		final Composite fileSelection = new Composite(shell, SWT.NULL);
		fileSelection.setLayoutData(GUIUtils.createFillGridData(1));
		fileSelection.setLayout(new GridLayout(4, false));

		final Label label = new Label(fileSelection, SWT.NONE);
		label.setText("Enter trivia file path: ");

		// text field to enter the file path
		final Text filePathField = new Text(fileSelection, SWT.SINGLE
				| SWT.BORDER);
		filePathField.setLayoutData(GUIUtils.createFillGridData(1));

		// "Browse" button 
		final Button browseButton = new Button(fileSelection,
				SWT.PUSH);
		browseButton.setText("Browse");
		browseButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e) {
				
				String fpath = GUIUtils.getFilePathFromFileDialog(shell);
				if(fpath.equals("Cancel") == false){
					filePathField.setText(fpath);
				}
			}
			
		});

		// "Play!" button
		final Button playButton = new Button(fileSelection, SWT.PUSH);
		playButton.setText("Play!");
		
		playButton.addSelectionListener(new SelectionAdapter(){
			public void widgetSelected(SelectionEvent e){
				
				triviaList = new ArrayList<>();
				triviaListIndex =0;
				currentScore =0;
				numOfWrongAnswers =0;
				questionCnt = 0;
				
				String f = filePathField.getText();   //the path to the file to read from
				Scanner sc = null;
				try{
					sc = new Scanner(new File(f));
					String line;
					//List<List<List<String>>> triviaList = new ArrayList<>();
					//<<<q1>, <ans1,ans2..>>>
					int rowNum = 0;
					while(sc.hasNextLine()){
						line = sc.nextLine();
						rowNum++;
						List<List<String>> qandAnswerList = new ArrayList<>();  //len ==2
						String[] strArr = line.split("\t");
						
						/*for(int j=0; j< strArr.length; j++){
							
							String str = strArr[j];
							char[] charArray = str.toCharArray();
							for(char c: charArray){
								if(c< 97)
							}
							
						}*/
						
						if(strArr.length ==0 || strArr.length != 5){
							String messageError = "Incorrect format in row "+ rowNum+ ". Should be "
									+ "question followed by 4 answers";
							GUIUtils.showErrorDialog(shell, messageError);
							throw new TriviaFileFormatException(messageError);
						}
						
						String q = strArr[0];
						if(((q.charAt(q.length()-1)) == ('?')) == false){
							String messageError = "Incorrect format in row "+ rowNum+ ": please add a  "
									+ "question mark.  Should be "
									+ "question followed by 4 answers";
							GUIUtils.showErrorDialog(shell, messageError);
							throw new TriviaFileFormatException(messageError);
							
						}
						
						List<String> question = new ArrayList<>();
						question.add(strArr[0]);
						qandAnswerList.add(question);
						List<String> answers = new ArrayList<>();
						for(int i=1; i<5; i++){
							answers.add(strArr[i]);
						}
						qandAnswerList.add(answers);
						triviaList.add(qandAnswerList);
					} //closes while loop
					
					Collections.shuffle(triviaList);
					if(triviaList.size() >0){
						String rightAnswer = new String(triviaList.get(0).get(1).get(0));
						Collections.shuffle((triviaList.get(0)).get(1)); //shuffle answers
						updateQuestionPanel((triviaList.get(0).get(0).get(0)), rightAnswer,
								(triviaList.get(0)).get(1));
					}
					
				} //end of try block
				catch(FileNotFoundException e2){
					e2.printStackTrace();
				}
				catch(IOException e1){
					e1.printStackTrace();
				} catch (TriviaFileFormatException t) {
					// TODO Auto-generated catch block
					 
						
				}
				finally{
					if(sc != null)
						sc.close();
				}
			} //end of widegetSelected
		});
	}

	/**
	 * Creates the panel that displays the current score
	 */
	private void createScorePanel() {
		Composite scorePanel = new Composite(shell, SWT.BORDER);
		scorePanel.setLayoutData(GUIUtils.createFillGridData(1));
		scorePanel.setLayout(new GridLayout(2, false));

		final Label label = new Label(scorePanel, SWT.NONE);
		label.setText("Total score: ");

		// The label which displays the score; initially empty
		scoreLabel = new Label(scorePanel, SWT.NONE);
		scoreLabel.setLayoutData(GUIUtils.createFillGridData(1));
	}

	/**
	 * Creates the panel that displays the questions, as soon as the game starts.
	 * See the updateQuestionPanel for creating the question and answer buttons
	 */
	private void createQuestionPanel() {
		questionPanel = new Composite(shell, SWT.BORDER);
		questionPanel.setLayoutData(new GridData(GridData.FILL,
				GridData.FILL, true, true));
		questionPanel.setLayout(new GridLayout(2, true));

		//Initially, only displays a message
		Label label = new Label(questionPanel, SWT.NONE);
		label.setText("No question to display, yet.");
		label.setLayoutData(GUIUtils.createFillGridData(2));
	}

	/**
	 * Serves to display the question and answer buttons
	 */
	private void updateQuestionPanel(String question, String rightAnswer,
			List<String> answers) {
		
		scoreLabel.setText(currentScore.toString());  //update the score
		
		// clear the question panel
		Control[] children = questionPanel.getChildren();
		for (Control control : children) {
			control.dispose();
		}

		// create the instruction label
		Label instructionLabel = new Label(questionPanel, SWT.CENTER
				| SWT.WRAP);
		instructionLabel.setText(lastAnswer 
				+ "Answer the following question:");
		instructionLabel
				.setLayoutData(GUIUtils.createFillGridData(2));

		// create the question label
		Label questionLabel = new Label(questionPanel, SWT.CENTER
				| SWT.WRAP);
		questionLabel.setText(question);
		questionLabel.setFont(boldFont);
		questionLabel.setLayoutData(GUIUtils.createFillGridData(2));

		// create the answer buttons
		
		//List<Button> answerButtonList = new ArrayList<Button>();
		
		for (int i = 0; i < 4; i++) {
			Button answerButton = new Button(questionPanel, SWT.PUSH
					| SWT.WRAP);
			answerButton.setText(answers.get(i));
			GridData answerLayoutData = GUIUtils
					.createFillGridData(1);
			answerLayoutData.verticalAlignment = SWT.FILL;
			answerButton.setLayoutData(answerLayoutData);
			
			answerButton.addSelectionListener(new SelectionAdapter(){
				
				public void widgetSelected(SelectionEvent e){
					
					if((triviaListIndex < triviaList.size()) &&
							(numOfWrongAnswers < MAX_ERRORS)){
						triviaListIndex++;
						questionCnt++;
						
						if((answerButton.getText().equals(rightAnswer))){
							currentScore = currentScore + 3;
							lastAnswer = "Correct! ";
						}
						
						else{ //wrong answer
							numOfWrongAnswers++;
							currentScore = currentScore -1;
							lastAnswer = "Wrong... ";
						}
						
						updateUtility();
						}
						
					}  //end of widget selected
			});
		}

		// create the "Pass" button to skip a question
		Button passButton = new Button(questionPanel, SWT.PUSH);
		passButton.setText("Pass");
		GridData data = new GridData(GridData.CENTER,
				GridData.CENTER, true, false);
		data.horizontalSpan = 2;
		passButton.setLayoutData(data);
		
		passButton.addSelectionListener(new SelectionAdapter(){
			
			public void widgetSelected(SelectionEvent e){
				
				if((triviaListIndex < triviaList.size()) &&
						(numOfWrongAnswers < MAX_ERRORS)){
					
					triviaListIndex++;
					lastAnswer = "";
					updateUtility();
				}
			}
		});

		// two operations to make the new widgets display properly
		questionPanel.pack();
		questionPanel.getParent().layout();
	}

	/**
	 * Opens the main window and executes the event loop of the application
	 */
	private void runApplication() {
		shell.open();
		Display display = shell.getDisplay();
		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
		boldFont.dispose();
	}
	
	//utility function
	
	private void updateUtility(){
		
		if((numOfWrongAnswers == (MAX_ERRORS)) || (triviaListIndex == triviaList.size()) ){
			
			GUIUtils.showInfoDialog(shell, "GAME OVER", "Your final"
					+ " score is "+ currentScore+ " after " + questionCnt
			+ " questions.");
		}
		
		
		else {  //we still have questions to ask
			String rightAnswer = new String(triviaList.get(triviaListIndex).get(1).get(0));
			Collections.shuffle((triviaList.get(triviaListIndex)).get(1)); //shuffle answers
			
			updateQuestionPanel((triviaList.get(triviaListIndex).get(0).get(0)), rightAnswer,
					(triviaList.get(triviaListIndex)).get(1));	
		}
	}
	
}
