from manim import *

class ArchitectureDiagram(Scene):
    def construct(self):
        # Define colors
        backend_color = BLUE
        database_color = GREEN
        redis_color = YELLOW
        arrow_color = WHITE
        text_color = WHITE

        # Create rectangles for Backend, Database, and Redis
        backend_rect = Rectangle(width=2.5, height=1.5, color=backend_color).shift(LEFT * 3)
        database_rect = Rectangle(width=2.5, height=1.5, color=database_color).shift(RIGHT * 3)
        redis_rect = Rectangle(width=2.5, height=1.5, color=redis_color).shift(DOWN * 2)

        # Create text labels for each component
        backend_text = Text("Backend", color=text_color).move_to(backend_rect.get_center())
        database_text = Text("Database", color=text_color).move_to(database_rect.get_center())
        redis_text = Text("Redis", color=text_color).move_to(redis_rect.get_center())

        # Create arrows to represent queries and data flow
        database_query_arrow = Arrow(backend_rect.get_right(), database_rect.get_left(), color=arrow_color, buff=0.2)
        redis_query_arrow = Arrow(backend_rect.get_bottom(), redis_rect.get_top(), color=arrow_color, buff=0.2)
        database_data_arrow = Arrow(database_rect.get_left(), backend_rect.get_right(), color=arrow_color, buff=0.2)
        redis_data_arrow = Arrow(redis_rect.get_top(), backend_rect.get_bottom(), color=arrow_color, buff=0.2)

        # Create text labels for the arrows
        database_query_text = Text("Database Query", color=text_color, font_size=20).move_to(database_query_arrow.get_center() + UP * 0.3)
        redis_query_text = Text("Redis Query", color=text_color, font_size=20).move_to(redis_query_arrow.get_center() + LEFT*0.5)
        database_data_text = Text("Data", color=text_color, font_size=20).move_to(database_data_arrow.get_center() + DOWN * 0.3)
        redis_data_text = Text("Data", color=text_color, font_size=20).move_to(redis_data_arrow.get_center() + RIGHT*0.5)

        # Create text for time
        database_time = Text("Longer Time", color=text_color).shift(RIGHT * 3 + UP * 2)
        redis_time = Text("Faster Time", color=text_color).shift(DOWN * 2 + LEFT * 3 + UP * 2)

        # Animate the creation of the diagram
        self.play(Create(backend_rect), Write(backend_text))
        self.play(Create(database_rect), Write(database_text))
        self.play(Create(redis_rect), Write(redis_text))
        self.play(Create(database_query_arrow), Write(database_query_text))
        self.play(Create(redis_query_arrow), Write(redis_query_text))
        self.play(Create(database_data_arrow), Write(database_data_text))
        self.play(Create(redis_data_arrow), Write(redis_data_text))
        self.play(Write(database_time))
        self.play(Write(redis_time))
        self.wait(3)
