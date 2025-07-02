from manim import *

class ArchitectureDiagram(Scene):
    def construct(self):
        # Define objects
        user = Rectangle(width=2, height=1.5).shift(LEFT * 4 + UP * 2).set_color(BLUE).set_fill(BLUE, opacity=0.5)
        backend = Rectangle(width=2, height=1.5).shift(LEFT * 4 + DOWN * 1).set_color(GREEN).set_fill(GREEN, opacity=0.5)
        redis = Rectangle(width=2, height=1.5).shift(RIGHT * 4 + DOWN * 1).set_color(YELLOW).set_fill(YELLOW, opacity=0.5)
        database = Rectangle(width=2, height=1.5).shift(RIGHT * 4 + UP * 2).set_color(RED).set_fill(RED, opacity=0.5)

        user_text = Text("User").scale(0.7).move_to(user)
        backend_text = Text("Backend").scale(0.7).move_to(backend)
        redis_text = Text("Redis").scale(0.7).move_to(redis)
        database_text = Text("Database").scale(0.7).move_to(database)

        # Add objects to the scene
        self.play(Create(user), Create(backend), Create(redis), Create(database))
        self.play(Write(user_text), Write(backend_text), Write(redis_text), Write(database_text))
        self.wait(0.5)

        # Connections (Arrows)
        arrow1 = Arrow(user.get_bottom(), backend.get_top(), buff=0.2).set_color(WHITE)
        text1 = Text("Request").scale(0.5).move_to(arrow1.get_center() + UP * 0.3)
        self.play(Create(arrow1), Write(text1))
        self.wait(0.3)

        arrow2 = Arrow(backend.get_right(), database.get_bottom(), buff=0.2).set_color(WHITE)
        text2 = Text("Query").scale(0.5).move_to(arrow2.get_center() + LEFT * 0.5)
        self.play(Create(arrow2), Write(text2))
        self.wait(0.3)

        arrow3 = Arrow(database.get_bottom(), backend.get_right(), buff=0.2).set_color(WHITE)
        text3 = Text("Data").scale(0.5).move_to(arrow3.get_center() + RIGHT * 0.5)
        self.play(Create(arrow3), Write(text3))
        self.wait(0.3)

        arrow4 = Arrow(backend.get_top(), user.get_bottom(), buff=0.2).set_color(WHITE)
        text4 = Text("Response (Slow)").scale(0.5).move_to(arrow4.get_center() + DOWN * 0.3)
        self.play(Create(arrow4), Write(text4))
        self.wait(0.5)

        # Redis Path
        arrow5 = Arrow(backend.get_left(), redis.get_top(), buff=0.2).set_color(WHITE)
        text5 = Text("Query").scale(0.5).move_to(arrow5.get_center() + UP * 0.3)
        self.play(Create(arrow5), Write(text5))
        self.wait(0.3)

        arrow6 = Arrow(redis.get_top(), backend.get_left(), buff=0.2).set_color(WHITE)
        text6 = Text("Data").scale(0.5).move_to(arrow6.get_center() + DOWN * 0.3)
        self.play(Create(arrow6), Write(text6))
        self.wait(0.3)

        arrow7 = Arrow(backend.get_top(), user.get_bottom(), buff=0.2).set_color(WHITE)
        text7 = Text("Response (Fast)").scale(0.5).move_to(arrow7.get_center() + DOWN * 0.3)
        self.play(Create(arrow7), Write(text7))
        self.wait(2)
