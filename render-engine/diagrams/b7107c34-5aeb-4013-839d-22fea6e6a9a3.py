from manim import *

class ArchitectureDiagram(Scene):
    def construct(self):
        # Define colors
        backend_color = BLUE
        redis_color = GREEN
        database_color = RED

        # Create rectangles for components
        backend = Rectangle(width=2, height=1, color=backend_color, fill_opacity=0.5).shift(LEFT * 3)
        redis = Rectangle(width=2, height=1, color=redis_color, fill_opacity=0.5).shift(RIGHT * 3 + UP*1.5)
        database = Rectangle(width=2, height=1, color=database_color, fill_opacity=0.5).shift(RIGHT * 3 + DOWN*1.5)

        # Create text labels
        backend_label = Text("Backend").move_to(backend.get_center())
        redis_label = Text("Redis").move_to(redis.get_center())
        database_label = Text("Database").move_to(database.get_center())

        # Create arrows
        database_query_arrow = Arrow(backend.get_right(), database.get_left(), buff=0.5)
        redis_query_arrow = DashedArrow(backend.get_right(), redis.get_left(), buff=0.5)

        # Create labels for arrows
        database_query_label = Text("Database Query").scale(0.7).next_to(database_query_arrow, UP)
        redis_query_label = Text("Redis Query").scale(0.7).next_to(redis_query_arrow, DOWN)
        
        #Create time labels
        database_time_label = Text("100ms").scale(0.6).next_to(database_query_arrow,DOWN)
        redis_time_label = Text("1ms").scale(0.6).next_to(redis_query_arrow,UP)

        # Animate
        self.play(Create(backend), Write(backend_label))
        self.play(Create(redis), Write(redis_label))
        self.play(Create(database), Write(database_label))
        self.wait(0.5)
        self.play(Create(database_query_arrow), Write(database_query_label))
        self.play(Write(database_time_label))
        self.wait(0.5)
        self.play(Create(redis_query_arrow), Write(redis_query_label))
        self.play(Write(redis_time_label))
        self.wait(2)

        self.camera.frame.save_state()
        self.play(self.camera.frame.animate.move_to(ORIGIN).scale(0.8))

        self.wait(2)
