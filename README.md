# RideTest
Test Red

App's capabilities: 

1. User can select a location on the map for there end destination 
2. Once the user hit start, the app start to track there route
3. Once either the user press finish or reach the destination a dialog appears to display all the information about the route. 
4. User can see there old routes in the history tab

Steps Taken: 

1. Research how the google map sdk works on Android
2. Look at tutorials using google map on Android
3. Look into Lifecycle Service and see how that can be used 
4. Figure out how to request location premission at runtime(which was harder then I thought since Google made some tweaks on the latest Android OS 
that was causing the permission not to request since it was just OS was just ignoring the old way I was doing it)
5. Set up Room database so I can save routes locally 
6. Figure out how to simluate location in the Android emulator 
7. Have a plan on how I would track the distance, at first I just used current user location and calcuated the
distance between that point the where the user was currently at but then relaize that was wrong since I could just go back
to my current location the distance was 0. So I just cacluated the distance between the last two points and added it to the distance variable. 
8. Also using those points I had to draw the route on the map
9. Figure out how to take an image of the route, which was easier then I thought it would be. 
10. Once the user finish just save the route then pass the id to the dialog. 
11. Inside the finish dialog do a quick query to display the route image, time and distance. 
12. After the user dismissed the dialog I clear the map and they can pick another location. 
13. In the history tab I just made a query to get all the routes and I display them a recycleview 

(PS if for some reason you get an error that said the BuildConfig file has been build twice just clean the project and re run the project) 

[![Screen-Shot-2021-11-18-at-10-29-16-AM.png](https://i.postimg.cc/dDMJ9qXL/Screen-Shot-2021-11-18-at-10-29-16-AM.png)](https://postimg.cc/FkZtrQh4)

[![Screen-Shot-2021-11-18-at-10-28-55-AM.png](https://i.postimg.cc/MZnRtdNS/Screen-Shot-2021-11-18-at-10-28-55-AM.png)](https://postimg.cc/DmK8wd7j)
