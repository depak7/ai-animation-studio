from manim import *

class ArchitectureDiagram(Scene):
    def construct(self):
        # Define Colors
        color_backend = BLUE_E
        color_redis = GREEN_E
        color_database = RED_E
        color_text = WHITE

        # Define Rectangles
        backend = Rectangle(width=2, height=1, color=color_backend, fill_opacity=1).shift(LEFT * 3)
        redis = Rectangle(width=2, height=1, color=color_redis, fill_opacity=1).shift(RIGHT * 3 + UP * 2)
        database = Rectangle(width=2, height=1, color=color_database, fill_opacity=1).shift(RIGHT * 3 + DOWN * 2)

        # Define Texts
        backend_text = Text("Backend", color=color_text).scale(0.7).move_to(backend.get_center())
        redis_text = Text("Redis", color=color_text).scale(0.7).move_to(redis.get_center())
        database_text = Text("Database", color=color_text).scale(0.7).move_to(database.get_center())

        # Define Arrows
        database_arrow = Arrow(backend.get_right(), database.get_left(), buff=0.5)
        redis_arrow = Arrow(backend.get_right(), redis.get_left(), buff=0.5)

        # Define Time Texts
        database_time = Text("100ms", color=color_text).scale(0.6).next_to(database_arrow, DOWN)
        redis_time = Text("10ms", color=color_text).scale(0.6).next_to(redis_arrow, UP)

        # Animations
        self.play(Create(backend), Write(backend_text))
        self.play(Create(redis), Write(redis_text))
        self.play(Create(database), Write(database_text))
        self.play(Create(database_arrow), Write(database_time))
        self.play(Create(redis_arrow), Write(redis_time))
        self.wait(2)

        # Highlight Redis Speed
        faster_text = Text("Redis provides faster response!").scale(0.8).to_edge(DOWN)
        self.play(Write(faster_text))
        self.wait(2)
