aspect_rectangle((Start-begin)*2, (Id-1)*2,((End-begin)*2), Id*2, (Color, foreground)) :- task(Task,Start,End), tasks(Id, Task, Color).
aspect_line((T-begin)*2, 0, (T-begin)*2, N*2, background) :- T=begin..end, n_tasks(N).
aspect_drawnode((T-begin)*2, (N*2)+1, T, "huge") :- T=begin..end, n_tasks(N).

aspect_style(green, "fill", "green").
aspect_style(green, "draw", "").
aspect_style(green, "very thick", "").

aspect_style(blue, "fill", "blue").
aspect_style(blue, "draw", "").
aspect_style(blue, "very thick", "").

aspect_style(red, "fill", "red").
aspect_style(red, "draw", "").
aspect_style(red, "very thick", "").

aspect_style(yellow, "fill", "yellow").
aspect_style(yellow, "draw", "").
aspect_style(yellow, "very thick", "").

aspect_layer(foreground, 1).
aspect_layer(background, 0).