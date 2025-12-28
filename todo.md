## sanity check 
- will this progress insights is really logical or any edge case where it won't work at all !

## enhancement
- Streak count in progress widget
    - We had introduced the streak count display in the progress widget, but with the new layout containing many text elements, would it be possible to simplify the display? Perhaps we could remove some text and focus on presenting the numbers in a more concise and readable format, such as a sentence structure. This would allow us to cover all key metrics in a single line.
    - The problem with the current approach is that the streak count is always calculated based on the difference being greater than zero, which adds 1 to the current streak. When a new day starts, the difference often shows as negative, and the streak count is displayed as `-`. This remains until enough habits are completed to make the difference positive, which can confuse users as they have no clue about their streak from yesterday or when the streak count will update.
    - Proposed solutions:
        - Example 1: "Today: 78.5% (+2.3% from yesterday), Streak: 5, Best: 12"  
          This combines most of the key metrics into a single sentence. However, the issue of the streak being shown as `-` until a certain number of habits are completed still persists.
        - Example 2: "P: 36.14906% (-0.88527%), CS: 5->5, BS: 10"  
          This format uses abbreviations for brevity:  
          - `P`: Today's average and difference from yesterday  
          - `CS`: Current streak till yesterday and projected streak based on today's progress  
          - `BS`: Best streak count
        - Example 3: "Progress: 78.5% (+2.3%), Current Streak: 5, Best Streak: 12"  
          This format is slightly more verbose but maintains clarity for the end user.
        - Example 4: "Current Progress: 78.5% (+2.3%), Streak: 5 (Yesterday: 5), Best: 12"  
          This explicitly shows the streak from yesterday alongside today's streak, providing more context to the user.
        - Example 5: "Today's Score: 36.14906%, Todays Progress till now: -0.88527%, Streak till yesterday: 5, Projected Streak: 0,  Best Streak: 10, Habits AUto COmpleted: 5, Habits Mnaully Skipped: 2, Habits coeplted: 25, Habis marked as not done:3, Habits pending: 20"  
          This format provides a comprehensive overview of the user's progress and streaks, including additional metrics like habits auto-completed, manually skipped, completed, marked as not done, and pending.
    - By adopting one of these formats, we can ensure that the information is both concise and contextually useful for the end user.

## bug
    - app crahs
      - after some inactivity time
      - some time even after the first alunch itself !
    - the 'did you know"' app hint comes in, is it a bug or the app is hard desinged to show this on ~2nd or 3rd day of the new verison usage ? [ harddesing doesn't make much sense, so i feel this si bug/feature we broke ]