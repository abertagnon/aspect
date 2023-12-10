aspect_fillrectangle((Start-begin)*2, (Id-1)*2,((End-begin)*2), Id*2, Color) :- task(Task,Start,End), tasks(Id, Task, Color).
aspect_drawline((T-begin)*2, 0, (T-begin)*2, N*2) :- T=begin..end, n_tasks(N).
aspect_drawnode((T-begin)*2, (N*2)+1, T, "huge") :- T=begin..end, n_tasks(N).