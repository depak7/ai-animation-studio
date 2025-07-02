from manim import *

class ArchitectureDiagram(Scene):
    def construct(self):
        # Define elements
        backend = Rectangle(width=2, height=1.5).shift(LEFT * 3).set_fill(BLUE, opacity=0.5).set_stroke(BLUE)
        backend_text = Text("Backend").scale(0.7).move_to(backend.get_center())
        database = Rectangle(width=2, height=1.5).shift(RIGHT * 3 + DOWN * 2).set_fill(GREEN, opacity=0.5).set_stroke(GREEN)
        database_text = Text("Database").scale(0.7).move_to(database.get_center())
        redis = Rectangle(width=2, height=1.5).shift(RIGHT * 3 + UP * 2).set_fill(YELLOW, opacity=0.5).set_stroke(YELLOW)
        redis_text = Text("Redis").scale(0.7).move_to(redis.get_center())
        title = Text("Faster Response with Redis").shift(UP * 3.5)

        db_query_arrow = Arrow(backend.get_right(), database.get_left(), buff=0.5)
        db_query_text = Text("DB Query").scale(0.6).move_to(db_query_arrow.get_center() + UP * 0.5)
        db_time_text = Text("Time: 3s").scale(0.5).next_to(db_query_text, DOWN)

        redis_query_arrow = Arrow(backend.get_right(), redis.get_left(), buff=0.5)
        redis_query_text = Text("Redis Query").scale(0.6).move_to(redis_query_arrow.get_center() + DOWN * 0.5)
        redis_time_text = Text("Time: 0.5s").scale(0.5).next_to(redis_query_text, UP)

        # Animations
        self.play(Write(title))
        self.wait(0.5)
        self.play(Create(backend), Write(backend_text))
        self.play(Create(database), Write(database_text))
        self.play(Create(redis), Write(redis_text))
        self.wait(0.5)

        self.play(Create(db_query_arrow), Write(db_query_text))
        self.play(Write(db_time_text))
        self.wait(1)
        self.play(Create(redis_query_arrow), Write(redis_query_text))
        self.play(Write(redis_time_text))

        self.wait(2)
        self.play(
            FadeOut(title),
            FadeOut(backend), FadeOut(backend_text),
            FadeOut(database), FadeOut(database_text),
            FadeOut(redis), FadeOut(redis_text),
            FadeOut(db_query_arrow), FadeOut(db_query_text), FadeOut(db_time_text),
            FadeOut(redis_query_arrow), FadeOut(redis_query_text), FadeOut(redis_time_text)
        )
        self.wait(0.5)
