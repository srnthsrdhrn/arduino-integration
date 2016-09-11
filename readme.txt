a settings page which is used to set the maximum value of RGB values

The Current RGB values and the values in settings must be stored in the database or file while closing.

The buttons:
1. S button
2. Shift button
3. New button
4. R, G, B bars, indicating the level 
5.A new graph to show the temperature readings recieved

I have a electronics project which uses arduino. it work like a counter . I have added a bluetooth module with it to cummunicate.

Now i want to develope an app like this counter and also sycronize with my electronic project . 
There are two buttons on device A and B . First we long press button A once then press button B many times and store the no of pressed value in X .
 LIke If I press 
button B 5 times then X=5 .
Second time again I long press button A once then press button B many times . This stores the value of pressed button in Y. Like if I press button B 6 times then Y = 6 .
Third time again I long press button A once then press button B many times . This stores the value of pressed button in Z. Like if I press button B 7 times then Z = 7 .
Now Values are 
X =5 Y=6 Z=7
means button A is use to select X or Y or Z, and button B is use to increse counter of selected 

Application Part 
I want an application which will have the same proess as device but it should have syncronisation with device through bluetooth.
when i connect my device to mobile and open app and do syncronisation . my hardware device send X , Y , Z value to mobile. app check if any of X,Y,Z is letest or big it 

update own value and if app have new (big value) so it send new value to device through bluetooth.
e.g. If my electronics device have value X=5 and then If I press button B through app then x should be start with 5 and on click it would increased like 6,7,8...

and a another independent value T is send by my deviece which is temperature of storage . App read and store temperature value T with time and will show graph of T 

values

App should contain:-
these X Y and z value show on a app page as a bar or anything which you like.
- A button to reset x y and z value to zero (every day i start new count)
- xyz value should persist after app close.
- Value of T should be stored for 30 days to make graph.
- i also need source code .