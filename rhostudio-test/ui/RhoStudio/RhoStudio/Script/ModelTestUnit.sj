function ModelTest01()
{
  //Runs the "RhoStudio" tested application.
  TestedApps.RhoStudio.Run();
  //Clicks the 'Button' button.
  Aliases.RhoStudio.Shell4.Composite.Composite.Composite.Composite.Button.ClickButton();
  //Moves the mouse cursor to the menu item specified and then simulates a single click.
  Aliases.RhoStudio.Shell2.MainMenu.Click("Window|Show View|Other...");
  //Clicks the '|General|Navigator' item of the 'Tree' tree.
  Aliases.RhoStudio.Shell7.Composite.Composite.FilteredTree.Composite.Tree.ClickItem("|General|Navigator");
  //Clicks the 'Button' button.
  Aliases.RhoStudio.Shell7.Composite.Composite1.Button.ClickButton();
  //Moves the mouse cursor to the menu item specified and then simulates a single click.
  Aliases.RhoStudio.Shell2.MainMenu.Click("File|New|Project...");
  //Clicks the '|RhoMobile|RhoMobile application' item of the 'Tree' tree.
  Aliases.RhoStudio.Shell5.Composite.Composite.Composite.Composite.Composite.Composite.Composite.FilteredTree.Composite.Tree.ClickItem("|RhoMobile|RhoMobile application");
  //Delays the test execution for the specified time period.
  Delay(500);
  //Clicks the 'Button' button.
  Aliases.RhoStudio.Shell5.Composite.Composite.Composite1.Composite.Composite.Button.ClickButton();
  //Clicks the 'Button' button.
  Aliases.RhoStudio.Shell6.Composite.Composite.Composite.Composite.Button.ClickButton();
  //Clicks at point (335, 11) of the 'CTabFolder' object.
  Aliases.RhoStudio.Shell2.Composite.Composite.CTabFolder.Click(335, 11);
  //Clicks the '|RhoMobileApplication1' item of the 'Tree' tree.
  Aliases.RhoStudio.Shell2.Composite.Composite.Composite.Composite.Tree.ClickItem("|RhoMobileApplication1");
  //Moves the mouse cursor to the menu item specified and then simulates a single click.
  Aliases.RhoStudio.Shell2.MainMenu.Click("File|New|Project...");
  //Clicks the 'Button' button.
  Aliases.RhoStudio.Shell5.Composite.Composite.Composite1.Composite.Button.ClickButton();
  //Moves the mouse cursor to the menu item specified and then simulates a single click.
  Aliases.RhoStudio.Shell2.MainMenu.Click("File|New|RhoMobile model");
  //Clicks at point (14, 12) of the 'Text' object.
  Aliases.RhoStudio.Shell6.Composite.Composite.Composite1.Composite.Composite.Text.Click(14, 12);
  //Enters the text 'a, b, c' in the 'Text' text editor.
  Aliases.RhoStudio.Shell6.Composite.Composite.Composite1.Composite.Composite.Text.SetText("a, b, c");
  //Clicks the 'Button' button.
  Aliases.RhoStudio.Shell6.Composite.Composite.Composite.Composite.Button.ClickButton();
  //Closes the 'Shell2' window.
  Aliases.RhoStudio.Shell2.Close();
  //Clicks the 'Button' button.
  Aliases.RhoStudio.Shell8.Composite.Button.ClickButton();
}