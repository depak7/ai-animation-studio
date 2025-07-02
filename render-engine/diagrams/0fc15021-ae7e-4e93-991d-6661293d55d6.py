from manim import *

class ArchitectureDiagram(Scene):
    def construct(self):
        # Define colors
        COLOR_RECT = BLUE
        COLOR_ARROW = WHITE
        COLOR_TEXT = WHITE

        # Create rectangles
        backend = Rectangle(width=2, height=1, color=COLOR_RECT).shift(UP * 2).set_fill(COLOR_RECT, opacity=0.5)
        database = Rectangle(width=2, height=1, color=COLOR_RECT).shift(DOWN * 1 + RIGHT * 4).set_fill(COLOR_RECT, opacity=0.5)
        redis = Rectangle(width=2, height=1, color=COLOR_RECT).shift(DOWN * 1 + LEFT * 4).set_fill(COLOR_RECT, opacity=0.5)

        # Create text labels
        backend_label = Text("Backend", color=COLOR_TEXT).move_to(backend.get_center())
        database_label = Text("Database", color=COLOR_TEXT).move_to(database.get_center())
        redis_label = Text("Redis", color=COLOR_TEXT).move_to(redis.get_center())

        # Create arrows
        backend_database_arrow = Arrow(backend.get_bottom(), database.get_top(), color=COLOR_ARROW, buff=0.2)
        backend_redis_arrow = Arrow(backend.get_bottom(), redis.get_top(), color=COLOR_ARROW, buff=0.2)

        database_query_label = Text("Database Query", color=COLOR_TEXT).next_to(backend_database_arrow, RIGHT, buff=0.1)
        redis_query_label = Text("Redis Query", color=COLOR_TEXT).next_to(backend_redis_arrow, LEFT, buff=0.1)

        # Create time labels
        database_time_label = Text("100ms", color=COLOR_TEXT).shift(RIGHT * 6)
        redis_time_label = Text("1ms", color=COLOR_TEXT).shift(LEFT * 6)

        # Animate the creation of the diagram
        self.play(Create(backend), Write(backend_label))
        self.play(Create(database), Write(database_label))
        self.play(Create(redis), Write(redis_label))

        self.play(Create(backend_database_arrow), Write(database_query_label))
        self.play(Create(backend_redis_arrow), Write(redis_query_label))

        self.play(Write(database_time_label))
        self.play(Write(redis_time_label))

        self.wait(3)
